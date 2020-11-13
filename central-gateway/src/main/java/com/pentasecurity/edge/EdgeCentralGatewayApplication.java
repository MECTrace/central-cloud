package com.pentasecurity.edge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class EdgeCentralGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(EdgeCentralGatewayApplication.class, args);
    }
}