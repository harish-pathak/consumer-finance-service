package com.infobeans.consumerfinance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Consumer Finance Service Application - Main Entry Point
 */
@SpringBootApplication
@EnableJpaAuditing
public class ConsumerFinanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerFinanceServiceApplication.class, args);
    }
}
