package com.gupao.spring.framework.beans.support;

import com.gupao.spring.framework.beans.config.GPBeanDefinition;
import com.gupao.spring.framework.context.support.GPAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GPDefaultListableBeanFactory extends GPAbstractApplicationContext {


    // 存储注册信息的BeanDefinition
    private final Map<String, GPBeanDefinition> beandefinitionMap = new ConcurrentHashMap<>(256);





}
