package io.github.livenne;

public interface Context {
    BeanFactory getBeanFactory();
    void loadModules(Application application);
}
