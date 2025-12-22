package io.github.livenne.module.servlet;

import io.github.livenne.annotation.servlet.*;
import io.github.livenne.utils.StringUtils;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ServiceServlet extends HttpServlet {

    private final Set<RouterMapping> routerMappingSet;
    private final Set<ExceptionHandle> exceptionHandleSet;

    public ServiceServlet(Set<RouterMapping> routerMappingSet, Set<ExceptionHandle> exceptionHandleSet) {
        this.routerMappingSet = routerMappingSet;
        this.exceptionHandleSet = exceptionHandleSet;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @SneakyThrows
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) {
        req.setCharacterEncoding("UTF-8");
        if (req.getMethod().equals("OPTIONS")) {
            return;
        }
        RouterMapping routerMapping = routerMatch(req, res);

        if (routerMapping == null){
            res.setStatus(404);
            return;
        }

        Object ret;
        try {
            ret = reqHandle(routerMapping, req, res);
        } catch (Exception e) {
            ret = exceptionHandle(e);
        }

        if (ret != null) {
            res.setContentType("application/json;charset=UTF-8");
            res.setCharacterEncoding("UTF-8");
            PrintWriter out = res.getWriter();
            out.println(StringUtils.toJson(ret));
        }

    }

    @SneakyThrows
    private RouterMapping routerMatch(HttpServletRequest req,HttpServletResponse res){
        for (RouterMapping routerMapping : routerMappingSet) {
            String requestPath = routerMapping.getRequestPath();
            String requestMethod = routerMapping.getRequestMethod();

            if (!requestMethod.equals(req.getMethod())) {
                continue;
            }
            if (!StringUtils.isPathEquals(req.getRequestURI(),requestPath)){
                continue;
            }
            return routerMapping;
        }
        return null;
    }

    private Object reqHandle(RouterMapping routerMapping,HttpServletRequest req,HttpServletResponse res) throws Exception{

        String requestPath = routerMapping.getRequestPath();
        Method method = routerMapping.getMethod();
        Object controller = routerMapping.getInstance();

        Map<String, String> pathVarMap = StringUtils.pathVarMap(req.getRequestURI(), requestPath);
        List<Object> paramList = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            Object arg = null;
            Class<?> type = parameter.getType();
            if (parameter.isAnnotationPresent(PathVariable.class)){
                String path = parameter.getAnnotation(PathVariable.class).value();
                arg = StringUtils.formJson(pathVarMap.get(path), type);
            }else if (parameter.isAnnotationPresent(RequestParm.class)){
                String parm =  parameter.getAnnotation(RequestParm.class).value();
                arg = StringUtils.formJson(req.getParameter(parm),type);
            } else if (parameter.isAnnotationPresent(Attribute.class)) {
                String attr = parameter.getAnnotation(Attribute.class).value();
                arg = req.getAttribute(attr);
            } else if (parameter.isAnnotationPresent(RequestBody.class) && method.isAnnotationPresent(PostMapping.class)) {
                arg = StringUtils.formJson(StringUtils.getBody(req),type);
            } else if (parameter.isAnnotationPresent(Request.class)){
                arg = req;
            } else  if (parameter.isAnnotationPresent(Response.class)){
                arg = res;
            }
            paramList.add(arg);
        }

        return method.invoke(controller, paramList.toArray());

    }

    private Object exceptionHandle(Exception e){
        ExceptionHandle exceptionHandle = exceptionHandleSet.stream()
                .filter(eh -> eh.getException().equals(e.getClass()))
                .findAny()
                .orElse(exceptionHandleSet.stream()
                        .filter(eh -> eh.getException().isAssignableFrom(e.getClass()))
                        .findAny().orElseThrow()
                );

        Object instance = exceptionHandle.getInstance();
        Method handler = exceptionHandle.getMethod();
        try {
            return handler.invoke(instance, e);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }
}