package io.github.livenne.utils;

import java.lang.annotation.Annotation;

public class AnnotationUtils {
    public static boolean isAnnotationPresent(Class<?> clazz, Class<? extends Annotation> targetAnnotation) {
        if (clazz.isAnnotationPresent(targetAnnotation)) {
            return true;
        }
        for (Annotation annotation : clazz.getAnnotations()) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (!annotationType.getName().startsWith("java.lang.annotation")) {
                return isAnnotationPresent(annotationType, targetAnnotation);
            }
        }
        return false;
    }
}
