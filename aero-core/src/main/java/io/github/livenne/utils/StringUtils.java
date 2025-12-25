package io.github.livenne.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;

@Slf4j
public class StringUtils{
    public static ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn(e.getMessage(),e.getCause());
            return null;
        }
    }

    public static <T> T formJson(String json,Class<T> clazz) {
        if (clazz.equals(String.class)){
            return clazz.cast(json);
        }
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            log.warn(e.getMessage(),e.getCause());
            return null;
        }
    }

    public static Boolean isPathVariable(String path) {
        return !path.isBlank() && path.startsWith("{") && path.endsWith("}");
    }

    public static Boolean isPathEquals(String req,String api){
        String[] reqArr = Arrays.stream(req.split("/")).filter(Predicate.not(String::isBlank)).toArray(String[]::new);
        String[] apiArr = Arrays.stream(api.split("/")).filter(Predicate.not(String::isBlank)).toArray(String[]::new);
        if (reqArr.length != apiArr.length) return false;
        for (int i = 0; i < reqArr.length; i++) {
            if (!reqArr[i].equals(apiArr[i]) && !isPathVariable(apiArr[i])) return false;
        }
        return true;
    }

    public static HashMap<String,String> pathVarMap(String req,String api){
        HashMap<String,String> map = new HashMap<>();
        if (!isPathEquals(req,api)) return map;
        String[] reqArr = Arrays.stream(req.split("/")).filter(Predicate.not(String::isBlank)).toArray(String[]::new);
        String[] apiArr = Arrays.stream(api.split("/")).filter(Predicate.not(String::isBlank)).toArray(String[]::new);
        for (int i = 0; i < reqArr.length; i++) {
            if (isPathVariable(apiArr[i])) {
                map.put(apiArr[i].substring(1,apiArr[i].length()-1), reqArr[i]);
            }
        }
        return map;
    }

    public static String getBody(HttpServletRequest req) {
        int contentLength = req.getContentLength();
        byte[] bytes = new byte[contentLength];

        try (ServletInputStream inputStream = req.getInputStream()) {
            int read = inputStream.read(bytes);
        } catch (IOException e) {
            log.error(e.getMessage(),e.getCause());
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
