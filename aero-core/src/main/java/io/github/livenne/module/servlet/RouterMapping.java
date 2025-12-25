package io.github.livenne.module.servlet;

import io.github.livenne.annotation.servlet.GetMapping;
import io.github.livenne.annotation.servlet.PostMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouterMapping {
    private Object instance;
    private String requestMethod;
    private String requestPath;
    private Method method;
    private List<Field> reqField;
    private List<Field> resField;

    public static final List<RouterMapping> mappingList = new ArrayList<>();

    public static boolean isRouterMapping(Method method){
        return method.isAnnotationPresent(GetMapping.class) || method.isAnnotationPresent(PostMapping.class);
    }
    public static String getRequestMethod(Method method){
        return method.isAnnotationPresent(GetMapping.class) ? "GET" : "POST";
    }

}
