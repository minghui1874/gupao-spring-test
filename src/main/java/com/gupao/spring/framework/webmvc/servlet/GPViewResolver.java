package com.gupao.spring.framework.webmvc.servlet;

import java.io.File;
import java.util.Locale;

public class GPViewResolver {


    private File templateRootDir;

    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";


    public GPViewResolver(String templateRoot) {
        String templateRootPath =  this.getClass().getClassLoader().getResource(templateRoot).getFile();
        this.templateRootDir = new File(templateRootPath);
    }


    public GPView resolveViewName(String viewName, Locale locale) throws Exception {
        if (null == viewName || "".equals(viewName.trim())) {return null;}
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX) ? viewName : viewName + DEFAULT_TEMPLATE_SUFFIX;

        File templateFile = new File(new String(templateRootDir.getPath() + "/" + viewName).replaceAll("/+", "/"));
        return new GPView(templateFile);
    }

//    public String getViewName() {
//        return viewName;
//    }
}
