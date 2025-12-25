package io.github.livenne.module.orm;

import io.github.livenne.*;
import io.github.livenne.Module;
import io.github.livenne.annotation.context.Component;
import io.github.livenne.annotation.context.PreDestroy;
import io.github.livenne.annotation.context.Value;
import io.github.livenne.annotation.orm.ColumnType;
import io.github.livenne.annotation.orm.Entity;
import io.github.livenne.annotation.orm.Id;
import io.github.livenne.annotation.orm.Repository;
import io.github.livenne.utils.AnnotationUtils;
import io.github.livenne.utils.ClassUtils;
import io.github.livenne.utils.SQLUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Component
public class ORMModule implements Module {

    @Value("database.url")
    private static String URL;
    @Value("database.driver")
    private static String DRIVER;
    @Value("database.username")
    private static String USERNAME;
    @Value("database.password")
    private static String PASSWORD;
    private static final Integer BASE_CONNECTION_POOL_SIZE = 8;
    private static ConcurrentLinkedQueue<Connection> connectionPool;
    private Context context;
    private BeanFactory beanFactory;
//    private static final ExecutorService executorService = Executors.newScheduledThreadPool(2);

    @Override
    public void load(Application application) {
        this.context = application.getApplicationContext();
        this.beanFactory = context.getBeanFactory();
        if (!connectDatabase()) {
            return;
        }

        this.beanFactory.getType(c->c.isAnnotationPresent(Entity.class)).forEach(this::automationDDL);
        ClassUtils.scanAllClass(application.getPrimarySources()).stream()
                .filter(c -> AnnotationUtils.isAnnotationPresent(c, Repository.class))
                .forEach(c->beanFactory.addBean(c.getName(),repositoryImpl(c)));
        beanFactory.autoWired();
    }

    @PreDestroy
    public void destroy(){
        connectionPool.forEach(connection -> {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean connectDatabase(){
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            log.error("Not found database driver",e.getCause());
            return false;
        }
        connectionPool = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < BASE_CONNECTION_POOL_SIZE; i++) {
            try {
                connectionPool.add(DriverManager.getConnection(URL, USERNAME, PASSWORD));
            } catch (SQLException e) {
                log.error("Database connect failure",e.getCause());
                return false;
            }
        }
        return true;
    }

    public static Object sqlExecutor(SqlFunction<Connection,Object> operation){

        Connection connection = getConnection();
        Object result = null;
        try {
            result = operation.apply(connection);
        } catch (SQLException e) {
            log.error(e.getMessage(),e.getCause());
        }
        returnConnection(connection);
        return result;
    }

    @SneakyThrows
    private static Connection getConnection() {
        if (connectionPool.isEmpty()) {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }
        Connection conn = connectionPool.poll();
        if (conn == null || conn.isClosed()|| !conn.isValid(3)){
            if (conn != null) {
                conn.close();
            }
            return getConnection();
        }
        return conn;
    }

    private static void returnConnection(Connection connection) {
        connectionPool.offer(connection);
    }

    @SneakyThrows
    public void automationDDL(Class<?> entityClass){
        String tableName = entityClass.getAnnotation(Entity.class).value();
        sqlExecutor(conn -> {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();
            try (ResultSet checkTable = metaData.getTables(catalog, null, tableName, new String[]{"TABLE"})){
                if (checkTable.next()){
                    tablePatch(entityClass,tableName);
                }else {
                    tableCreate(entityClass,tableName);
                }
                return null;
            }
        });
    }

    private void tablePatch(Class<?> entity,String tableName) {
        for (Field field : entity.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            sqlExecutor(conn -> {
                DatabaseMetaData metaData = conn.getMetaData();
                try (ResultSet checkColumn = metaData.getColumns(null, null, tableName, fieldName)){
                    if (!checkColumn.next()) {
                        StringJoiner alterColumnSql = new StringJoiner(" ");
                        alterColumnSql
                                .add("ALTER TABLE")
                                .add(tableName)
                                .add("ADD COLUMN")
                                .add(fieldName)
                                .add(field.isAnnotationPresent(ColumnType.class) ?
                                        field.getAnnotation(ColumnType.class).value() :
                                        SQLUtils.typeMatch(field.getType()))
                                .add(";");
                        try (PreparedStatement preparedStatement = conn.prepareStatement(alterColumnSql.toString())){
                            preparedStatement.execute();
                        }
                    }
                    return null;

                }
            });
        }
    }

    private void tableCreate(Class<?> entity,String tableName){
        StringJoiner createTableSql = new StringJoiner(",","CREATE TABLE IF NOT EXISTS " + tableName + " (",");");
        for (Field field : entity.getDeclaredFields()) {
            field.setAccessible(true);
            StringJoiner columnJoiner = new StringJoiner(" ");
            columnJoiner
                    .add(field.getName())
                    .add(field.isAnnotationPresent(ColumnType.class) ?
                            field.getAnnotation(ColumnType.class).value() :
                            SQLUtils.typeMatch(field.getType()))
                    .add(field.isAnnotationPresent(Id.class) ? field.getAnnotation(Id.class).value().getValue() + " PRIMARY KEY" : "");
            createTableSql.add(columnJoiner.toString());
        }
        sqlExecutor(conn->{
            try (PreparedStatement statement = conn.prepareStatement(createTableSql.toString())){
                statement.execute();
                return null;
            }
        });
    }

    public Object repositoryImpl(Class<?> repoClass) {
        return Proxy.newProxyInstance(
                repoClass.getClassLoader(),
                new Class[]{repoClass}, new SQLMethodProxy(repoClass));
    }


}
