package com.gupao.spring.framework.aop;

import com.gupao.spring.framework.aop.support.GPAdvisedSupport;

public class GPCglibAopProxy implements GPAopProxy {

    public GPCglibAopProxy(GPAdvisedSupport config) {

    }

    @Override
    public Object getProxy() {

        return null;
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }
}
