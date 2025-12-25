package io.github.livenne;

import java.util.Set;
import java.util.function.Predicate;

public interface BeanFactory{

    BeanDefinition getBeanDefinition(String name);

    void addBean(String name,Object bean);

    Object getBean(String name);
    Object getBean(Class<?> type);

    <T> Set<T> getBeans(Class<T> type);
    Set<Object> getBeans(Predicate<Object> predicate);

    Set<Class<?>> getType(Class<?> type);

    Set<Class<?>> getType(Predicate<Class<?>> predicate);

    void autoWired();
}
