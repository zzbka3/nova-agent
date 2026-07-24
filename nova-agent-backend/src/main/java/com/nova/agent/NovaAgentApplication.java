package com.nova.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.nova.agent.repository")
@EnableScheduling
public class NovaAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovaAgentApplication.class, args);
    }
}
