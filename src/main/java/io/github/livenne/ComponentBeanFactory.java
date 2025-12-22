package io.github.livenne;

import io.github.livenne.annotation.context.Component;
import io.github.livenne.annotation.context.Value;
import io.github.livenne.utils.AnnotationUtils;
import io.github.livenne.utils.ClassUtils;
import io.github.livenne.utils.StringUtils;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ComponentBeanFactory implements BeanFactory {

    private final Set<Class<?>> classSet;
    private final Set<BeanDefinition> beanDefinitionSet;
    private final Map<String,Object> beanMap;

    private final ApplicationProperties properties;
    private final Set<Class<?>> sources;

    public ComponentBeanFactory(Application application){
        this.beanMap = new HashMap<>();
        this.properties = application.getApplicationProperties();
        this.sources = application.getPrimarySources();
        this.classSet = ClassUtils.scanAllClass(sources).stream().filter(this::canScan).collect(Collectors.toSet());
        this.beanDefinitionSet = classSet.stream().map(BeanDefinition::new).collect(Collectors.toSet());
        createBeans();
        autoWired();
        configurationInject();
        postConstruct();
    }

    public void createBeans(){
        beanDefinitionSet.forEach(bf -> {
            try {
                addBean(bf.getName(), bf.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void autoWired() {
        beanDefinitionSet.forEach(bf->{
            Object bean = getBean(bf.getName());
            for (Field field : bf.getAutowiredFields()) {
                field.setAccessible(true);
                try {
                    field.set(bean,getBean(field.getType()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void configurationInject() {
        beanDefinitionSet.forEach(bf->{
            Object bean = getBean(bf.getName());
            for (Field field : bf.getConfigurationFields()) {
                field.setAccessible(true);
                String value = properties.getProperties().getProperty(field.getAnnotation(Value.class).value());
                if (value == null){
                    value = field.getAnnotation(Value.class).defValue();
                }
                try {
                    field.set(bean, StringUtils.formJson(value,field.getType()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void postConstruct() {
        this.beanDefinitionSet.forEach(bf->{
            Object bean = getBean(bf.getName());
            for (Method method : bf.getPostConstructMethods()) {
                method.setAccessible(true);
                try {
                    method.invoke(bean);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    protected boolean canScan(Class<?> clazz){
        return ClassUtils.isNormalClass(clazz) && AnnotationUtils.isAnnotationPresent(clazz, Component.class);
    }

    @Override
    public BeanDefinition getBeanDefinition(String name) {
        return beanDefinitionSet.stream().filter(bf->bf.getName().equals(name)).findAny().orElse(null);
    }

    @Override
    public void addBean(String name, Object bean) {
        beanMap.put(name, bean);
    }

    @Override
    public Object getBean(String name) {
        return beanMap.get(name);
    }

    @Override
    public Object getBean(Class<?> type) {
        return beanMap.values().stream().filter(type::isInstance).findAny().orElse(null);
    }

    @Override
    public <T> Set<T> getBeans(Class<T> type) {
        return beanMap.values()
                .stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Object> getBeans(Predicate<Object> predicate) {
        return beanMap.values()
                .stream()
                .filter(predicate)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Class<?>> getType(Class<?> type) {
        return classSet.stream().filter(type::isInstance).collect(Collectors.toSet());
    }

    @Override
    public Set<Class<?>> getType(Predicate<Class<?>> predicate) {
        return classSet.stream().filter(predicate).collect(Collectors.toSet());
    }
}
