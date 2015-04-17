package com.adidas.poc;

import org.springframework.boot.SpringApplication;

/**
 *
 */
public class UDIApplication {
    public static void main(String[] args) {
        initApplication(args);
    }

    private static void initApplication(String[] args) {
        SpringApplication.run(UDIApplicationConfiguration.class, args);

    }
}
