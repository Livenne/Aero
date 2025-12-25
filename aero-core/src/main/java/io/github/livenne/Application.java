package io.github.livenne;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class Application {

    private final ApplicationProperties applicationProperties;
    private final Set<Class<?>> primarySources;
    private final Context applicationContext;
    private final Set<Runnable> shutdownHooks;

    public Application(Set<Class<?>> primarySources){
        this.primarySources = primarySources;
        this.shutdownHooks = new HashSet<>();
        this.applicationProperties = new ApplicationProperties();
        this.applicationContext = new ApplicationContext(this);
    }

    public static Context run(Class<?>... primarySource){
        Set<Class<?>> sources = Arrays.stream(primarySource).collect(Collectors.toSet());
        sources.add(Application.class);
        return new Application(sources).run();
    }

    public Context run(){
        this.applicationContext.loadModules(this);
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            Thread.currentThread().setName("Shutdown-Thread");
            log.info("Shutdown hook running");
            shutdownHooks.forEach(Runnable::run);
            log.info("Shutdown successfully");
        }));
        return applicationContext;
    }

    public void addShutdownHook(Runnable runnable) {
        shutdownHooks.add(runnable);
    }
}
