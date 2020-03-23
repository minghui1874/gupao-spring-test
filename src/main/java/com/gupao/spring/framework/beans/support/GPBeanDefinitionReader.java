
package com.gupao.spring.framework.beans.support;

import com.gupao.spring.framework.annotation.GPComponent;
import com.gupao.spring.framework.beans.config.GPBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GPBeanDefinitionReader {

    List<String> registryBeanClass = new ArrayList<>();

    private Properties config = new Properties();

    //固定配置文件中的key，相当于xml中的规范
    private final String SCANNER_PACKAGE = "scanPackage";

    public GPBeanDefinitionReader(String[] configLocations) {

        // 通过URL 定位找到所对应的文件，转化为文件流读取成properties
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(configLocations[0].replace("classpath:", ""))) {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        doScanner(config.getProperty(SCANNER_PACKAGE).toString());


    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = scanPackage + "." + file.getName().replace(".class", "");
                registryBeanClass.add(className);
            }
        }
    }


    public List<GPBeanDefinition> loadBeanDefinitions() {
        List<GPBeanDefinition> result = new ArrayList<>();
        for (String className : registryBeanClass) {
            GPBeanDefinition beanDefinition = doCreateBeanDefinition(className);
            if (null != beanDefinition) {
                result.add(beanDefinition);
            }
        }
        return result;
    }


    /**
     * 把每一个配置信息解析成一个beanDefinition
     *
     * @param className
     * @return
     */
    private GPBeanDefinition doCreateBeanDefinition(String className) {

        try {
            Class<?> beanClass = Class.forName(className);

            if (!checkHasComponentAnnotation(beanClass.getAnnotations())) {
                return null;

            }
            // 有可能是一个接口,用他的实现类作为beanClassName
            if (beanClass.isInterface()) {
                return null;

            }
            GPBeanDefinition beanDefinition = new GPBeanDefinition();
            beanDefinition.setBeanClassName(className);
            beanDefinition.setFactoryBeanName(this.toLowerFirstCase(beanClass.getSimpleName()));
            return beanDefinition;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }

    private boolean checkHasComponentAnnotation(Annotation[] annotations) {
        boolean hasComponentAnnotation = false;
        for (Annotation annotation : annotations) {
            if (!hasComponentAnnotation) {
                hasComponentAnnotation = annotation.annotationType().isAnnotationPresent(GPComponent.class);
            }
        }
        if (!hasComponentAnnotation) {
            for (Annotation annotation : annotations) {
                hasComponentAnnotation = checkHasComponentAnnotation(annotation.getClass().getAnnotations());
            }
        }
        return hasComponentAnnotation;
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


    public Properties getConfig() {
        return config;
    }
}
