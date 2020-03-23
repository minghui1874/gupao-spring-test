package com.gupao.spring.framework.beans;

import lombok.Data;

@Data
public class GPBeanWrapper {

    private Object wrappedInstance;
    private Class<?> wrappedClass;


    public GPBeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
        this.wrappedClass = wrappedInstance.getClass();
    }



    public Object getWrappedInstance() {
        return this.wrappedInstance;
    }

    public Class<?> getWrappedClass() {
        return this.wrappedClass;
    }

}
