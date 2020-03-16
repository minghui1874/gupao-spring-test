package com.gupao.spring.framework.context;

import com.gupao.spring.framework.beans.GPBeanFactory;
import com.gupao.spring.framework.beans.support.GPDefaultListableBeanFactory;

public class GPApplicationContext extends GPDefaultListableBeanFactory implements GPBeanFactory {

    @Override
    public Object getBean(String beanName) {
        return null;
    }


}
