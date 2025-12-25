package io.github.livenne.module.servlet;

import io.github.livenne.Application;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.Servlet;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Slf4j
public class TomcatServer {
    private final Tomcat tomcat;
    private final int port;
    private final Servlet servlet;
    private final Application application;
    private Thread thread;
    @Getter
    private Context context;

    public TomcatServer(int port, Servlet servlet, Application application) {
        this.application = application;
        this.tomcat = new Tomcat();
        this.port = port;
        this.servlet = servlet;
    }

    @SneakyThrows
    public void start(){
        application.addShutdownHook(() -> {
            try {
                tomcat.stop();
                tomcat.destroy();
            } catch (LifecycleException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }
        });
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        tomcat.setPort(port);
        String tempDir = createTempDir();
        tomcat.setBaseDir(tempDir);
        context = tomcat.addContext("", tempDir);

        File uploadTempDir = new File(tempDir, "upload-tmp");
        if (!uploadTempDir.exists()) {
            Files.createDirectory(uploadTempDir.toPath());
        }
        Wrapper wrapper = Tomcat.addServlet(context, "ServiceServlet", servlet);
        wrapper.setMultipartConfigElement(
                new MultipartConfigElement(
                        uploadTempDir.getAbsolutePath(),
                        10 * 1024 * 1024,
                        50 * 1024 * 1024,
                        1024 * 1024
                )
        );
        context.setAllowCasualMultipartParsing(true);
        context.addServletMappingDecoded("/*", "ServiceServlet");
        thread = new Thread(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
            tomcat.getConnector();
            tomcat.start();
            tomcat.getServer().await();
        }});
        thread.setName("Tomcat-Server-Main");
        thread.start();
    }

    public void stop() {
        try {
            tomcat.getServer().stop();
        } catch (LifecycleException e) {
            log.error(e.getMessage(),e.getCause());
        }
    }

    private String createTempDir() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "framework-tomcat");
        if (!tempDir.exists()) {
            Files.createDirectory(tempDir.toPath());
        }
        return tempDir.getAbsolutePath();
    }
}
