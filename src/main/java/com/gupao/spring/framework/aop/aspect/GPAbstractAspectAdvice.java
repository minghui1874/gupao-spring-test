package com.gupao.spring.framework.aop.aspect;

import java.lang.reflect.Method;

public abstract class GPAbstractAspectAdvice {
    private Method aspectMethod;
    private Object aspectTarget;

    public GPAbstractAspectAdvice(Method aspectMethod, Object aspectTarget) {
        this.aspectMethod = aspectMethod;
        this.aspectTarget = aspectTarget;
    }
}
