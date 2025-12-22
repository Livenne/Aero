package io.github.livenne;

import io.github.livenne.annotation.context.Autowired;
import io.github.livenne.annotation.context.PostConstruct;
import io.github.livenne.annotation.context.Value;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

@Getter
public class BeanDefinition {
    private final String name;
    private final Class<?> classType;
    private final Constructor<?> constructor;
    private final Method[] postConstructMethods;
    private final Field[] autowiredFields;
    private final Field[] configurationFields;

    @SneakyThrows
    public BeanDefinition(Class<?> clazz){
        this.name = clazz.getName();
        this.classType = clazz;
        this.constructor = clazz.getConstructor();
        this.autowiredFields = Arrays
                .stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Autowired.class))
                .toArray(Field[]::new);
        this.postConstructMethods = Arrays
                .stream(clazz.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(PostConstruct.class))
                .toArray(Method[]::new);
        this.configurationFields = Arrays
                .stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Value.class))
                .toArray(Field[]::new);
    }
}
