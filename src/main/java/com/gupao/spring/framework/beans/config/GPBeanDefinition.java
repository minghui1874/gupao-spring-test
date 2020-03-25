package com.gupao.spring.framework.beans.config;

public class GPBeanDefinition {


    // bean的class name
    private String beanClassName;
    // 是否延迟加载
    private boolean lazyInit = false;

    // 工厂中的beanName
    private String factoryBeanName;

    // 是否单例
    private boolean isSingleton = true;


    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public boolean isSingleton() {
        return isSingleton;
    }

    public void setSingleton(boolean singleton) {
        isSingleton = singleton;
    }
}
