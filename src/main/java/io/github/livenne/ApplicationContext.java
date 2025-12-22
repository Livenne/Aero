package io.github.livenne;

import java.util.Set;

public class ApplicationContext implements Context {

    private final BeanFactory beanFactory;

    public ApplicationContext(Application application){
        this.beanFactory = new ComponentBeanFactory(application);
    }

    @Override
    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    @Override
    public void loadModules(Application application) {
        getModules().forEach(module -> module.load(application));
    }


    public Set<Module> getModules(){
        return beanFactory.getBeans(Module.class);
    }

}
