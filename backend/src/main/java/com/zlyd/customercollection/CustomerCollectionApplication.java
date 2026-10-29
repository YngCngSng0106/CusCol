package com.zlyd.customercollection;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan("com.zlyd.customercollection.mapper")
@SpringBootApplication
public class CustomerCollectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerCollectionApplication.class, args);
    }
}
