package io.github.livenne.utils;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.HashSet;
import java.util.Set;

public class ClassUtils {
    public static Set<Class<?>> scanAllClass(Class<?> clazz) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(clazz.getPackageName(), clazz.getClassLoader()))
                .setScanners(Scanners.SubTypes.filterResultsBy(s -> true))
        );
        return new HashSet<>(reflections.getSubTypesOf(Object.class));
    }

    public static Set<Class<?>> scanAllClass(Set<Class<?>> clazzArray){
        Set<Class<?>> classSet = new HashSet<>();
        clazzArray.stream().map(ClassUtils::scanAllClass).forEach(classSet::addAll);
        return classSet;
    }

    public static boolean isNormalClass(Class<?> clazz) {
        return !clazz.isInterface()
                && !clazz.isArray()
                && !clazz.isEnum()
                && !clazz.isPrimitive()
                && !clazz.isAnnotation();
    }
}
