package com.predicto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PredictoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PredictoApplication.class, args);
    }
}
