package io.github.livenne.utils;

public class SQLUtils {
    public static String typeMatch(Class<?> type){
        if (type.equals(String.class)) return "TEXT";
        if (type.equals(byte.class)) return "TINYINT";
        if (type.equals(Byte.class)) return "TINYINT";
        if (type.equals(char.class)) return "TINYINT";
        if (type.equals(Character.class)) return "TINYINT";
        if (type.equals(short.class)) return "SMALLINT";
        if (type.equals(Short.class)) return "SMALLINT";
        if (type.equals(int.class)) return "INT";
        if (type.equals(Integer.class)) return "INT";
        if (type.equals(long.class)) return "BIGINT";
        if (type.equals(Long.class)) return "BIGINT";
        if (type.equals(float.class)) return "FLOAT";
        if (type.equals(Float.class)) return "FLOAT";
        if (type.equals(double.class)) return "DOUBLE";
        if (type.equals(Double.class)) return "DOUBLE";
        if (type.equals(boolean.class)) return "TINYINT";
        if (type.equals(Boolean.class)) return "TINYINT";
        return "VARCHAR(255)";
    }
}
