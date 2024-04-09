package com.jcca.web.ibmMQ.support;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;


@Component("beanLocator")
public class BeanLocator implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    public <T> T findBean(String beanName, Class<T> type) {
        return (T) this.applicationContext.getBean(beanName, type);
    }


    public <T> T findBean(Class<T> type) {
        return (T) this.applicationContext.getBean(type);
    }


    public Collection<Object> findBeansAnnotatedWith(Class<? extends Annotation> annotation) {
        return this.applicationContext.getBeansWithAnnotation(annotation).values();
    }




    public <T> Collection<T> findBeans(Class<T> type) {
        return this.applicationContext.getBeansOfType(type).values();
    }


    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}