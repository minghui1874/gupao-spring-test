package com.gupao.spring.framework.context;

import com.gupao.spring.framework.beans.GPBeanFactory;
import com.gupao.spring.framework.beans.config.GPBeanDefinition;
import com.gupao.spring.framework.beans.support.GPBeanDefinitionReader;
import com.gupao.spring.framework.beans.support.GPDefaultListableBeanFactory;

import java.util.List;
import java.util.Map;

public class GPApplicationContext extends GPDefaultListableBeanFactory implements GPBeanFactory {

    private String[] configLocations;
    private GPBeanDefinitionReader reader;

    public GPApplicationContext(String[] configLocations) {
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
                getBean(beanName);
            }
        }



    }

    private void doRegisterBeanDefinition(List<GPBeanDefinition> beanDefinitions) {
        for (GPBeanDefinition beanDefinition : beanDefinitions) {
            super.beandefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }
    }

    @Override
    public Object getBean(String beanName) {
        return null;
    }


}
