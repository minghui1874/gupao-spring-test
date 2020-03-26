package com.gupao.spring.framework.servlet.v2;


import com.gupao.spring.framework.annotation.GPController;
import com.gupao.spring.framework.annotation.GPService;
import com.gupao.spring.framework.annotation.GpAutowired;
import com.gupao.spring.framework.annotation.GPRequestMapping;
import com.gupao.spring.framework.annotation.GPRequestParam;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class GPDispatcherServlet extends HttpServlet {

    // 保存配置文件中的配置内容
    private Properties contextConfig = new Properties();

    // 保存扫描到的所有类名
    private List<String> classNames = new ArrayList<>();

    // IOC容器
    private Map<String, Object> ioc = new HashMap<>();

    // 保存url和method的对应关系
    private Map<String, Method> handlerMapping = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    // 运行阶段
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 调用
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception , Detail : " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
    }


    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //绝对路径
        String url = req.getRequestURI();

        //处理成相对路径
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 NOT Found!!!");
            return;
        }

        Method method = this.handlerMapping.get(url);


        // 获取实参
        Map<String, String[]> params = req.getParameterMap();

        // 获取方法形参
        Class<?>[] parameterTypes = method.getParameterTypes();

        Object[] paramValues = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            // 不能用instanceof  parameterTypes 是形参不是实参，不能直接判断类型
            if (parameterType == HttpServletRequest.class) {
                paramValues[i] = req;
                continue;
            } else if (parameterType == HttpServletResponse.class) {
                paramValues[i] = resp;
                continue;
            } else {
                // 把方法上的注解拿到，获取到一个二维数组
                // 同一个方法可能有多个参数，且同一个参数上可以添加多个注解
                // 所以是二维数组
                Annotation[][] annotations = method.getParameterAnnotations();
                for (int j = 0; j < annotations.length; j++) {
                    for (Annotation annotation : annotations[j]) {
                        if (annotation instanceof GPRequestParam) {
                            String paramName = ((GPRequestParam) annotation).value();
                            if (!"".equals(paramName.trim())) {
                                if (params.containsKey(paramName)) {
//                                    for (Map.Entry<String, String[]> param : params.entrySet()) {
                                    String value = Arrays.toString(params.get(paramName)).replaceAll("\\[|\\]", "")
                                            .replaceAll("\\s", ",");

                                    paramValues[i] = convert(parameterType, value);
//                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        // 通过反射拿到method所在的class，拿到class 之后再拿到class名称
        //在调用首字母小写方法获取beanName
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());

        method.invoke(ioc.get(beanName), paramValues);
    }

    // url传过来的参数都是String类型的 ，HTTP是基于字符串协议
    // 只需要把String转换为对应类型就好
    private Object convert(Class<?> type, String value) {
        // 可以使用策略模式优化
        if (Integer.class == type) {
            return Integer.valueOf(value);
        } else if (Double.class == type) {
            return Double.valueOf(value);
        }

        return value;
    }

    // 初始化阶段
    @Override
    public void init(ServletConfig config) throws ServletException {

        // 1. 加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        // 2. 扫描相关的了类
        doScanner(contextConfig.getProperty("scanPackage"));

        // 3. 初始化扫描到的类，并且将它们放入到IOC容器之中
        doInstance();

        // 4. 完成依赖注入
        doAutowired();

        // 5. 初始化handlerMapping
        initHandlerMapping();

        System.out.println("GP Spring framework is init");


    }

    // 初始化 url 和 Method的一对一对应关系
    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }

        Class<GPRequestMapping> requestMappingClass = GPRequestMapping.class;
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();

            if (!clazz.isAnnotationPresent(GPController.class)) {
                continue;
            }

            String baseUrl = "";
            if (clazz.isAnnotationPresent(requestMappingClass)) {
                GPRequestMapping requestMapping = clazz.getAnnotation(requestMappingClass);
                baseUrl = requestMapping.value();
            }

            // 获取所有的public方法
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(requestMappingClass)) {
                    continue;
                }
                GPRequestMapping requestMapping = method.getAnnotation(requestMappingClass);
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                handlerMapping.put(url, method);
                System.out.println("Mapped : " + url + "," + method);
            }
        }


    }

    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            // declared 这个声明可以
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(GpAutowired.class)) {
                    continue;
                }
                GpAutowired autowired = field.getAnnotation(GpAutowired.class);

                // 如果用户没有自定义beanName，就根据类型注入
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) {
                    // 获取接口类型，作为key， 通过key从ioc容器中取值
                    beanName = field.getType().getName();
                }

                // 如果是public以外的修饰符，只要加了 Autowired注解，都要强制赋值
                field.setAccessible(true);
                try {
                    // 使用反射，动态给字段赋值
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void doInstance() {

        // 初始化，为DI做准备
        if (classNames.isEmpty()) {
            return;
        }

        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);

                //加了注解的类才初始化，
                if (clazz.isAnnotationPresent(GPController.class)) {
                    Object instance = clazz.newInstance();
                    // 类名首字母小写的beanName
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(GPService.class)) {

                    // 1. 自定义的baneName
                    GPService service = clazz.getAnnotation(GPService.class);
                    String beanName = service.value();

                    // 2. 默认类名首字母小写
                    if ("".equals(beanName.trim())) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }


                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    // 3. 根据类型自动赋值
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The “ " + i.getName() + " ” is exists");
                        }
                        ioc.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 小写首字母
     *
     * @param simpleName
     * @return
     */
    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * 扫描出相关的类
     *
     * @param scanPackage
     */
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = scanPackage + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }


    /**
     * 加载配置文件
     *
     * @param contextConfigLocation
     */
    private void doLoadConfig(String contextConfigLocation) {
        // 直接从类路径下找到Spring的主配置文件所在的路径并将其读取出来放入properties对象中
        try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation)) {
            contextConfig.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
