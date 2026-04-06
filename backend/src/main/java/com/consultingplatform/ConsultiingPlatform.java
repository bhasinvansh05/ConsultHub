package com.consultingplatform;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConsultiingPlatform {

    public static void main(String[] args) {
        // Try loading from current directory first, fallback to parent directory
        Dotenv dotenv = Dotenv.configure()
                .directory("../")
                .ignoreIfMissing()
                .load();
        
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );
        
        SpringApplication.run(ConsultiingPlatform.class, args);
    }
}
