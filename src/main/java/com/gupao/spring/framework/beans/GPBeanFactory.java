package com.gupao.spring.framework.beans;

/**
 * 单例工厂的顶层抽象
 */
public interface GPBeanFactory {


    /**
     * get bean by name from ioc container
     * @param beanName
     * @return
     */
    Object getBean(String beanName);


}
