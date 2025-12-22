package io.github.livenne.module.servlet;

import io.github.livenne.*;
import io.github.livenne.Module;
import io.github.livenne.annotation.context.Component;
import io.github.livenne.annotation.context.Value;
import io.github.livenne.annotation.servlet.ExceptionHandler;
import io.github.livenne.annotation.servlet.Controller;
import io.github.livenne.annotation.servlet.ControllerAdvice;
import io.github.livenne.annotation.servlet.GetMapping;
import io.github.livenne.annotation.servlet.PostMapping;
import io.github.livenne.utils.AnnotationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.LifecycleException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@Component
public class ServletModule implements Module {

    private ApplicationProperties properties;
    @Value(value = "server.port",defValue = "8080")
    private int port;
    private Context context;
    private BeanFactory beanFactory;
    private Set<Object> controllerSet;
    private Set<Object> controllerAdviceSet;
    private final Set<RouterMapping>  routerMappingSet = new HashSet<>();
    private final Set<ExceptionHandle> exceptionHandleSet = new HashSet<>();

    @Override
    public void load(Application application) {
        this.properties = application.getApplicationProperties();
        this.context = application.getApplicationContext();
        this.beanFactory = context.getBeanFactory();
        this.controllerSet = this.beanFactory.getBeans(o->AnnotationUtils.isAnnotationPresent(o.getClass(), Controller.class));
        this.controllerSet.forEach(this::routerMethodMapping);
        this.controllerAdviceSet = this.beanFactory.getBeans(o -> AnnotationUtils.isAnnotationPresent(o.getClass(), ControllerAdvice.class));
        this.controllerAdviceSet.forEach(this::controllerAdviceSetMapping);
        TomcatServer tomcatServer = new TomcatServer(port,new ServiceServlet(routerMappingSet, exceptionHandleSet));
        try {
            tomcatServer.start();
        } catch (IOException | LifecycleException e) {
            throw new RuntimeException(e.getMessage(),e.getCause());
        }
    }


    public void routerMethodMapping(Object controller) {
        String apiHead = controller.getClass().getAnnotation(Controller.class).value();
        List<Field> reqField = new ArrayList<>();
        List<Field> resField = new ArrayList<>();
        Field[] autowiredFields = beanFactory.getBeanDefinition(controller.getClass().getName())
                .getAutowiredFields();
        Arrays.stream(autowiredFields)
                .filter(field -> HttpServletRequest.class.isAssignableFrom(field.getType()))
                .forEach(reqField::add);
        Arrays.stream(autowiredFields)
                .filter(field -> HttpServletResponse.class.isAssignableFrom(field.getType()))
                .forEach(resField::add);
        for (Method method : controller.getClass().getDeclaredMethods()) {
            String mapPath = apiHead;
            if (method.isAnnotationPresent(GetMapping.class)){
                mapPath += method.getAnnotation(GetMapping.class).value();
            }else if (method.isAnnotationPresent(PostMapping.class)){
                mapPath += method.getAnnotation(PostMapping.class).value();
            }else {
                continue;
            }
            routerMappingSet.add(new RouterMapping(controller, RouterMapping.getRequestMethod(method), mapPath, method,reqField,resField));
        }

    }

    public void controllerAdviceSetMapping(Object controllerAdvice){

        Arrays.stream(controllerAdvice.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(ExceptionHandler.class))
                .forEach(method -> exceptionHandleSet.add(new ExceptionHandle(
                        method.getAnnotation(ExceptionHandler.class).value(),
                        controllerAdvice,
                        method)));
    }

}
