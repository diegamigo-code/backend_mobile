package com.wim100.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Wim100BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Wim100BackendApplication.class, args);
    }
}
