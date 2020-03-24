package com.gupao.spring.framework.webmvc.servlet;

import com.gupao.spring.framework.annotation.GPController;
import com.gupao.spring.framework.annotation.GpRequestMapping;
import com.gupao.spring.framework.context.GPApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class GPDispatcherServlet extends HttpServlet {

    private GPApplicationContext context;

    private String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    private List<GPHandlerMapping> handlerMappings = new ArrayList<>();

    private List<GPViewResolver> viewResolvers = new ArrayList<>();

    private Map<GPHandlerMapping, GPHandlerAdapter> handlerAdapters = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 调用
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
//            new GPModelAndView("500");
            resp.getWriter().write("500 Exception , Detail : " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        // 1. 通过从request中拿到url，去匹配一个handlerMapping
        GPHandlerMapping handler = getHandler(req);

        if (handler == null) {
//            new GPModelAndView("404");
            return;
        }

        // 2.   准备调用前的参数
        GPHandlerAdapter ha = getHandlerAdapter(handler);

        // 3. 调用方法, 返回GPModelAndView存储了要传到页面上的值和页面模板的名称
        GPModelAndView view = ha.handle(req, resp, handler);

        // 4. 渲染视图
        processDispatchResult(req, resp, view);

    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, GPModelAndView view) {

        // 把给我的ModelAndView变成一个HTML、OutStream。json等
        // Context-Type
        if (null == view) {
            return;
        }


    }

    private GPHandlerAdapter getHandlerAdapter(GPHandlerMapping handler) {
        return null;
    }

    private GPHandlerMapping getHandler(HttpServletRequest req) throws Exception {
        if (handlerMappings.isEmpty()) {
            return null;
        }

        String contextPath = req.getContextPath();
        String url = req.getRequestURI().replace(contextPath, "").replaceAll("/+", "/");
        // 找出handlerMapping中匹配的url
        return this.handlerMappings.stream().filter(mapping -> mapping.getPattern().matcher(url).matches()).findAny().orElse(null);
    }


    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1. 初始化ApplicationContext
        context = new GPApplicationContext(config.getInitParameter(CONTEXT_CONFIG_LOCATION));

        // 2. 初始化Spring MVC 九大组件
        this.initStrategies(context);


    }

    // 初始化策略
    protected void initStrategies(GPApplicationContext context) {
        // 多文件上传的组件
        initMultipartResolver(context);
        // 初始化本地预言环境
        initLocaleResolver(context);
        // 初始化模板处理器
        initThemeResolver(context);
        // handlerMapping
        initHandlerMappings(context);
        // 初始化参数适配器
        initHandlerAdapters(context);
        // 初始化异常拦截器
        initHandlerExceptionResolvers(context);
        //初始化视图预处理器
        initRequestToViewNameTranslator(context);
        // 初始化视图转换器
        initViewResolvers(context);
        // 参数缓存器
        initFlashMapManager(context);
    }

    private void initFlashMapManager(GPApplicationContext context) {

    }

    private void initViewResolvers(GPApplicationContext context) {

        // 拿到模板的存放目录
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        File templateRootDir = new File(templateRootPath);
        for (File template : templateRootDir.listFiles()) {
            // 这里主要是为了兼容多模板，所以模仿Spring用list保存
            this.viewResolvers.add(new GPViewResolver(templateRoot));
        }

    }

    private void initRequestToViewNameTranslator(GPApplicationContext context) {

    }

    private void initHandlerExceptionResolvers(GPApplicationContext context) {

    }

    private void initHandlerAdapters(GPApplicationContext context) {
        // 把一个request请求变成一个handler，参数都是字符串的，自动匹配到handler中的形参
        // 意味着有几个handlerMapping就有几个HandlerAdapter
        for (GPHandlerMapping handlerMapping : this.handlerMappings) {
            this.handlerAdapters.put(handlerMapping, new GPHandlerAdapter());

        }

    }

    private void initHandlerMappings(GPApplicationContext context) {
        String[] beanDefinitionNames = context.getBeanDefinitionNames();
        try {
            for (String beanName : beanDefinitionNames) {
                Object controller = context.getBean(beanName);
                Class<?> clazz = controller.getClass();

                if (!clazz.isAnnotationPresent(GPController.class)) {
                    continue;
                }


                String baseUrl = "";
                Class<GpRequestMapping> requestMappingClass = GpRequestMapping.class;
                // 获取controller的url配置
                if (clazz.isAnnotationPresent(requestMappingClass)) {
                    GpRequestMapping requestMapping = clazz.getAnnotation(requestMappingClass);
                    baseUrl = requestMapping.value();
                }

                // 获取所有的public方法
                // 获取Method的url配置
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    // 没有加RequestMapping注解的 直接忽略
                    if (!method.isAnnotationPresent(requestMappingClass)) {
                        continue;
                    }
                    // 获取映射的URL
                    GpRequestMapping requestMapping = method.getAnnotation(requestMappingClass);
                    String regex = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("\\*", ".*")
                            .replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);
                    this.handlerMappings.add(new GPHandlerMapping(pattern, controller, method));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initThemeResolver(GPApplicationContext context) {

    }

    private void initLocaleResolver(GPApplicationContext context) {

    }

    private void initMultipartResolver(GPApplicationContext context) {
    }
}
