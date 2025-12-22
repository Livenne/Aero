package io.github.livenne;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
@Getter
public class ApplicationProperties {
    private final Properties properties;

    public ApplicationProperties(){
        this.properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(Constant.CONFIG_FILE_NAME);
        if (inputStream == null){
            throw new RuntimeException("File not found: " + Constant.CONFIG_FILE_NAME);
        }
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + Constant.CONFIG_FILE_NAME,e.getCause());
        }
    }
}
