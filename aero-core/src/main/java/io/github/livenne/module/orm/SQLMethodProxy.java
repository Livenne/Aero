package io.github.livenne.module.orm;

import io.github.livenne.BaseMapper;
import io.github.livenne.MatchType;
import io.github.livenne.annotation.orm.*;
import io.github.livenne.utils.ObjectUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

@Slf4j
public class SQLMethodProxy implements InvocationHandler {

    private final Class<?> repoClass;

    public SQLMethodProxy(Class<?> repoClass) {
        this.repoClass = repoClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return switch (method.getName()) {
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> {
                    if (args != null && args.length == 1) {
                        yield proxy == args[0];
                    }
                    yield false;
                }
                case "toString" -> "SQLProxy for " + repoClass.getName();
                default -> method.invoke(proxy, args);
            };
        }

        Class<?> mapperType = getSuperClass(repoClass);
        if (!mapperType.isAnnotationPresent(Entity.class)){
            return ObjectUtils.getDefaultValue(method.getReturnType());
        }
        String tableName = mapperType.getAnnotation(Entity.class).value();
        Boolean isCollection = Collection.class.isAssignableFrom(method.getReturnType());
        Object result = null;

        Parameter[] parameters = method.getParameters();
        Map.Entry<String, List<Object>> entry = conditionGenerate(parameters, args);
        String condition = entry.getKey();
        List<Object> condValueList = entry.getValue();
        Map<String,Object> updateMap = updateMapGenerate(parameters,args);
        if (method.isAnnotationPresent(Insert.class)){
            result = insertSqlImpl(tableName,args[0]);
        }
        if (method.isAnnotationPresent(Delete.class)){
            result = deleteSqlImpl(tableName, condition,condValueList);
        }
        if (method.isAnnotationPresent(Update.class)){
            result = updateSqlImpl(tableName,condition,condValueList,updateMap);
        }
        if (method.isAnnotationPresent(Query.class)){
            result = querySqlImpl(tableName, condition,condValueList,mapperType,isCollection);
        }

        if (method.getReturnType().equals(Void.class) || method.getReturnType().equals(void.class)){
            return null;
        }
        return Optional.ofNullable(result).orElse(ObjectUtils.getDefaultValue(method.getReturnType()));
    }


    private Class<?> getSuperClass(Class<?> cls) {
        return (Class<?>) Arrays.stream(cls.getGenericInterfaces())
                .map(t -> (ParameterizedType) t)
                .filter(type -> type.getRawType().equals(BaseMapper.class))
                .map(ParameterizedType::getActualTypeArguments)
                .findFirst().orElseThrow()[0];
    }

    private Map.Entry<String,List<Object>> conditionGenerate(Parameter[] parameters, Object[] args) {
        StringJoiner condition = new StringJoiner(" AND ");
        List<Object> valueList = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            if (!parameters[i].isAnnotationPresent(Cond.class)) {
                continue;
            }
            Cond cond = parameters[i].getAnnotation(Cond.class);
            String column = cond.value();
            String value = "?";

            valueList.add(cond.type().equals(MatchType.LIKE) ? "%" + args[i] + "%" : args[i]);
            StringJoiner entry = new StringJoiner(" ");
            entry.add(column).add(cond.type().toString()).add(value);
            condition.add(entry.toString());
        }
        return Map.entry(condition.toString(),valueList);
    }

    private Map<String, Object> updateMapGenerate(Parameter[] parameters, Object[] args) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            if (!parameters[i].isAnnotationPresent(Column.class)){
                continue;
            }
            if (ObjectUtils.isBasicType(parameters[i].getType())) {
                map.put(parameters[i].getAnnotation(Column.class).value(),args[i]);
            }else {
                Object obj = args[i];
                for (Field field : obj.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    map.put(field.getName(),field.get(obj));
                }
            }
        }
        return map;
    }


    private Object insertSqlImpl(String tableName, Object arg) throws IllegalAccessException {
        StringJoiner sql = new StringJoiner(" ");
        List<Object> valueList = new ArrayList<>();
        sql.add("INSERT").add("INTO").add(tableName);
        StringJoiner columns = new StringJoiner(",","(",")");
        StringJoiner values = new StringJoiner(",","(",")");
        for (Field field : arg.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            columns.add(field.getName());
            values.add("?");
            valueList.add(field.get(arg));
        }
        sql.add(columns.toString()).add("VALUES").add(values.toString());

        return ORMModule.sqlExecutor(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)){
                for (int i = 0; i < valueList.size(); i++) {
                    statement.setObject(i+1,valueList.get(i));
                }
                statement.executeUpdate();
                try (ResultSet resultSet = statement.getGeneratedKeys()){
                    if (resultSet.next()) {
                        return resultSet.getLong(1);
                    }
                }
                return null;
            }
        });
    }

    private Object deleteSqlImpl(String tableName, String condition, List<Object> condValueList) {
        StringJoiner sql = new StringJoiner(" ");
        sql.add("DELETE").add("FROM").add(tableName);
        if (condition.isBlank()){
            log.warn("Delete condition is empty");
        }else {
            sql.add("WHERE").add(condition);
        }
        return ORMModule.sqlExecutor(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql.toString())){
                for (int i = 0; i < condValueList.size(); i++) {
                    statement.setObject(i+1,condValueList.get(i));
                }
                statement.executeUpdate();
                return true;
            }
        });
    }

    private Object updateSqlImpl(String tableName, String condition,List<Object> condValueList, Map<String, Object> updateMap) {
        StringJoiner sql = new StringJoiner(" ");
        List<Object> valueList = new ArrayList<>();
        sql.add("UPDATE").add(tableName);
        if (updateMap.isEmpty()){
            return false;
        }
        StringJoiner update = new StringJoiner(",");
        updateMap.forEach((column,value)->{
            StringJoiner entry = new StringJoiner(" = ");
            entry.add(column).add("?");
            valueList.add(value);
            update.add(entry.toString());
        });
        sql.add("SET").add(update.toString());
        if (condition.isBlank()){
            log.warn("Update condition is empty");
        }else {
            sql.add("WHERE").add(condition);
        }
        valueList.addAll(condValueList);
        return ORMModule.sqlExecutor(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql.toString())){
                for (int i = 0; i < valueList.size(); i++) {
                    statement.setObject(i+1,valueList.get(i));
                }
                statement.executeUpdate();
                return true;
            }
        });
    }


    private Object querySqlImpl(String tableName, String condition,List<Object> condValueList,Class<?> mapperType,Boolean isCollection) {
        StringJoiner sql = new StringJoiner(" ");
        sql.add("SELECT").add("*").add("FROM").add(tableName);
        if (!condition.isBlank()){
            sql.add("WHERE").add(condition);
        }
        return ORMModule.sqlExecutor(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql.toString())){
                for (int i = 0; i < condValueList.size(); i++) {
                    statement.setObject(i+1,condValueList.get(i));
                }
                ResultSet resultSet = statement.executeQuery();
                if (isCollection){
                    List<Object> list = new ArrayList<>();
                    while (resultSet.next()){
                        list.add(mapperToObject(mapperType, resultSet));
                    }
                    return list;
                }
                if (resultSet.next()) {
                    return mapperToObject(mapperType,resultSet);
                }else {
                    return null;
                }
            }
        });

    }

    @SneakyThrows
    public static Object mapperToObject(Class<?> mapperType,ResultSet resultSet) {
        Object object = mapperType.getConstructor().newInstance();
        for (Field field : mapperType.getDeclaredFields()) {
            field.setAccessible(true);
            field.set(object,resultSet.getObject(field.getName()));
        }
        return object;
    }
}
