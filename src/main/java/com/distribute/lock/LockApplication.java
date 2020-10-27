package com.distribute.lock;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@MapperScan("com.distribute.lock.mapper")
@SpringBootApplication
public class LockApplication {
    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(LockApplication.class, args);
    }
}
