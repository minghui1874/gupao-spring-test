package com.gupao.spring.framework.context;

import com.gupao.spring.framework.annotation.GPController;
import com.gupao.spring.framework.annotation.GPService;
import com.gupao.spring.framework.annotation.GpAutowired;
import com.gupao.spring.framework.beans.GPBeanFactory;
import com.gupao.spring.framework.beans.GPBeanWrapper;
import com.gupao.spring.framework.beans.config.GPBeanDefinition;
import com.gupao.spring.framework.beans.config.GPBeanPostProcessor;
import com.gupao.spring.framework.beans.support.GPBeanDefinitionReader;
import com.gupao.spring.framework.beans.support.GPDefaultListableBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class GPApplicationContext extends GPDefaultListableBeanFactory implements GPBeanFactory {

    private String[] configLocations;
    private GPBeanDefinitionReader reader;

    // 单例的IOC容器
    private Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    // 通用的IOC容器
    private Map<String, GPBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

    public GPApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        this.refresh();
    }

    @Override
    public void refresh() {
        // 1. 定位 配置文件
        reader = new GPBeanDefinitionReader(configLocations);

        // 2. 加载配置文件，扫描相关的类，把它们封装成BeanDefinition
        List<GPBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();


        // 3. 把配置信息注册到容器中(伪IOC容器)
        doRegisterBeanDefinition(beanDefinitions);


        // 4. 把不是延时加载的类提前初始化
        doAutoWired();

    }

    // 只处理非延时加载的情况
    private void doAutoWired() {
        for (Map.Entry<String, GPBeanDefinition> beanDefinitionEntry : super.beandefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if (!beanDefinitionEntry.getValue().isLazyInit()) {
                try {
                    getBean(beanName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private void doRegisterBeanDefinition(List<GPBeanDefinition> beanDefinitions) {
        for (GPBeanDefinition beanDefinition : beanDefinitions) {
            super.beandefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }
    }

    public Object getBean(Class<?> clazz) throws Exception {
        return this.getBean(clazz.getName());
    }

    @Override
    public Object getBean(String beanName) throws Exception {

        GPBeanDefinition beanDefinition = this.beandefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new RuntimeException("bean not found ：" + beanName);
        }

        Object instance = instantiateBean(beanName, beanDefinition);

        GPBeanPostProcessor processor = new GPBeanPostProcessor();

        // 前置通知
        processor.postProcessBeforeInitialization(instance, beanName);

        // 1. 初始化
        // 把反射获取的对象封装到BeanWrapper中
        GPBeanWrapper beanWrapper = new GPBeanWrapper(instance);

        // 2. 拿到beanWrapper之后，把beanWrapper保存到IOC容器中
        factoryBeanInstanceCache.put(beanName, beanWrapper);

        // 后置通知
        processor.postProcessAfterInitialization(instance, beanName);

        // 3. 注入
        populateBean(beanName, beanDefinition, beanWrapper);

        return factoryBeanInstanceCache.get(beanName);
    }

    private void populateBean(String beanName, GPBeanDefinition gpBeanDefinition, GPBeanWrapper gpBeanWrapper) {
        if (gpBeanWrapper == null) {
            return;
        }
        Object instance = gpBeanWrapper.getWrappedInstance();

        // 判断只有加了注解的类，财智星依赖注入
        Class<?> beanClass = gpBeanWrapper.getWrappedClass();
        if (!beanClass.isAnnotationPresent(GPController.class) && !beanClass.isAnnotationPresent(GPService.class)) {
            return;
        }

        // 获得所有的fields
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(GpAutowired.class)) {
                continue;
            }

            // 获取autowired注解信息
            GpAutowired autowired = field.getAnnotation(GpAutowired.class);
            String autowiredBeanName = autowired.value().trim();
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }

            // 授权
            field.setAccessible(true);

            //赋值
            Optional.ofNullable(this.factoryBeanInstanceCache.get(autowiredBeanName)).map(GPBeanWrapper::getWrappedInstance).ifPresent(obj
                    -> {
                try {
                    field.set(instance, obj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });


        }


    }

    private Object instantiateBean(String beanName, GPBeanDefinition gpBeanDefinition) {
        if (gpBeanDefinition == null) {
            return null;
        }
        // 1. 拿到要实例化的对象类名
        String beanClassName = gpBeanDefinition.getBeanClassName();

        // 2. 反射实例化
        Object instance = null;
        try {
            if (this.singletonObjects.containsKey(beanClassName)) {
                instance = this.singletonObjects.get(beanClassName);
            } else {

                Class<?> clazz = Class.forName(beanClassName);
                instance = clazz.newInstance();
                this.singletonObjects.put(beanClassName, instance);
                this.singletonObjects.put(gpBeanDefinition.getFactoryBeanName(), instance);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return instance;

    }

    public String[] getBeanDefinitionNames() {
        return this.beandefinitionMap.keySet().toArray(new String[this.beandefinitionMap.size()]);
    }

    public int getBeanDefinitionCount() {
        return this.beandefinitionMap.size();
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }


}
