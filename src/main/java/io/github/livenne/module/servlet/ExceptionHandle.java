package io.github.livenne.module.servlet;

import lombok.Getter;

import java.lang.reflect.Method;

@Getter
public class ExceptionHandle {
    private final Class<? extends Exception> exception;
    private final Object instance;
    private final Method method;

    public ExceptionHandle(Class<? extends Exception> exception, Object instance, Method method) {
        this.exception = exception;
        this.instance = instance;
        this.method = method;
    }
}
