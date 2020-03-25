
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


    //把配置文件中扫描到的所有的配置信息转换为GPBeanDefinition对象，以便于之后IOC操作方便
    public List<GPBeanDefinition> loadBeanDefinitions() {
        List<GPBeanDefinition> result = new ArrayList<>();
        try {
            for (String className : registryBeanClass) {
                Class<?> beanClass = Class.forName(className);
                //如果是一个接口，是不能实例化的
                //用它实现类来实例化
                if(beanClass.isInterface()) { continue; }

                //beanName有三种情况:
                //1、默认是类名首字母小写
                //2、自定义名字
                //3、接口注入
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()),beanClass.getName()));
//                result.add(doCreateBeanDefinition(beanClass.getName(),beanClass.getName()));

                Class<?> [] interfaces = beanClass.getInterfaces();
                for (Class<?> i : interfaces) {
                    //如果是多个实现类，只能覆盖
                    //为什么？因为Spring没那么智能，就是这么傻
                    //这个时候，可以自定义名字
                    result.add(doCreateBeanDefinition(i.getName(),beanClass.getName()));
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 把每一个配置信息解析成一个beanDefinition
     *
     * @param beanClassName
     * @return
     */
    private GPBeanDefinition doCreateBeanDefinition(String factoryBeanName,String beanClassName) {

        GPBeanDefinition beanDefinition = new GPBeanDefinition();
        beanDefinition.setBeanClassName(beanClassName);
        beanDefinition.setFactoryBeanName(factoryBeanName);
        return beanDefinition;

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
