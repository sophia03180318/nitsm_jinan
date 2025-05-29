package com.jcca.dataProcessing.support;

public interface IListener<T> {
    void onEvent(T event) throws Exception;
}
