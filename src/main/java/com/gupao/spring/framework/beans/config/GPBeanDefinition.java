package com.gupao.spring.framework.beans.config;

import lombok.Data;

@Data
public class GPBeanDefinition {


    // bean的class name
    private String beanClassName;
    // 是否延迟加载
    private boolean lazyInit = false;

    // 工厂中的beanName
    private String factoryBeanName;

}
