package com.gupao.spring.framework.servlet.v3;


import com.gupao.spring.framework.annotation.GPController;
import com.gupao.spring.framework.annotation.GPService;
import com.gupao.spring.framework.annotation.GpAutowired;
import com.gupao.spring.framework.annotation.GpRequestMapping;
import com.gupao.spring.framework.annotation.GpRequestParam;

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
//    private Map<String, Method> handlerMapping = new HashMap<>();
    // 根据设计原则：单一职责，最少知道原则
    private List<HandlerMapping> handlerMapping = new ArrayList<>();

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

        HandlerMapping handlerMapping = getHandler(req);
        if (handlerMapping == null) {
            resp.getWriter().write("404 NOT Found!!!");
            return;
        }

        Method method = handlerMapping.getMethod();
        // 获取方法形参列表
        Class<?>[] paramTypes = handlerMapping.getParamTypes();

        Object[] paramValues = new Object[paramTypes.length];

        Map<String, String[]> params = req.getParameterMap();
        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "")
                    .replaceAll("\\s", ",");

            if (!handlerMapping.paramIndexMapping.containsKey(param.getKey())) {
                continue;
            }

            int index = handlerMapping.getParamIndexMapping().get(param.getKey());
            paramValues[index] = convert(paramTypes[index], value);

        }

        // 处理req的参数位置
        Integer reqIndex = handlerMapping.paramIndexMapping.get(HttpServletRequest.class.getName());
        paramValues[reqIndex] = req;
        // 处理req的参数位置
        Integer respIndex = handlerMapping.paramIndexMapping.get(HttpServletResponse.class.getName());
        paramValues[respIndex] = resp;

        handlerMapping.method.invoke(handlerMapping.controller, paramValues);

    }

    private HandlerMapping getHandler(HttpServletRequest req) {
        if (handlerMapping.isEmpty()) {
            return null;
        }

        // 绝对路径
        String url = req.getRequestURI();

        //处理成相对路径
        String contextPath = req.getContextPath();
        String finalUrl = url.replaceAll(contextPath, "").replaceAll("/+", "/");
        // 找出handlerMapping中匹配的url
        return this.handlerMapping.stream().filter(mapping -> mapping.getUrl().equals(finalUrl)).findAny().orElse(null);
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

        Class<GpRequestMapping> requestMappingClass = GpRequestMapping.class;
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();

            if (!clazz.isAnnotationPresent(GPController.class)) {
                continue;
            }

            String baseUrl = "";
            if (clazz.isAnnotationPresent(requestMappingClass)) {
                GpRequestMapping requestMapping = clazz.getAnnotation(requestMappingClass);
                baseUrl = requestMapping.name();
            }

            // 获取所有的public方法
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(requestMappingClass)) {
                    continue;
                }
                GpRequestMapping requestMapping = method.getAnnotation(requestMappingClass);
                String url = ("/" + baseUrl + "/" + requestMapping.name()).replaceAll("/+", "/");
                this.handlerMapping.add(new HandlerMapping(url, entry.getValue(), method));
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

    // 保存一个url和一个Method的关系
    public class HandlerMapping {

        private String url;

        private Method method;

        private Object controller;

        private Class<?>[] paramTypes;

        // 形参列表
        // 参数名作为key， 参数的位置作为value
        private Map<String, Integer> paramIndexMapping;

        public HandlerMapping(String url, Object controller, Method method) {
            this.url = url;
            this.method = method;
            this.controller = controller;


            paramTypes = method.getParameterTypes();
            paramIndexMapping = new HashMap<>();
            pubParamIndexMapping(method);
        }

        private void pubParamIndexMapping(Method method) {

            Annotation[][] annotations = method.getParameterAnnotations();
            for (int i = 0; i < annotations.length; i++) {
                for (Annotation annotation : annotations[i]) {
                    // 只解析GPRequestParam
                    if (annotation instanceof GpRequestParam) {
                        // 拿到注解修饰下的参数名称，去和url上的参数名进行匹配
                        String paramName = ((GpRequestParam) annotation).value();
                        if (!"".equals(paramName.trim())) {
                            // 保存参数位置
                            paramIndexMapping.put(paramName, i);
                        }
                    }
                }
            }

            // 获取方法中的request和response参数
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> type = parameterTypes[i];
                // 保存request和response的参数位置
                if (type == HttpServletRequest.class ||
                        type == HttpServletResponse.class) {
                    paramIndexMapping.put(type.getName(), i);
                }
            }

        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public Object getController() {
            return controller;
        }

        public void setController(Object controller) {
            this.controller = controller;
        }

        public Map<String, Integer> getParamIndexMapping() {
            return paramIndexMapping;
        }

        public void setParamIndexMapping(Map<String, Integer> paramIndexMapping) {
            this.paramIndexMapping = paramIndexMapping;
        }

        public Class<?>[] getParamTypes() {
            return paramTypes;
        }

        public void setParamTypes(Class<?>[] paramTypes) {
            this.paramTypes = paramTypes;
        }
    }
}
