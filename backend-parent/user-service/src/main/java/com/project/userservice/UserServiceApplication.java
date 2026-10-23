package com.project.userservice;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        resolveModuleDir();
        SpringApplication.run(UserServiceApplication.class, args);
    }

    // target/classes -> user-service folder, so the db file location ignores the working directory
    private static void resolveModuleDir() {
        try {
            Path classes = Paths.get(UserServiceApplication.class
                    .getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isDirectory(classes) && classes.getParent() != null) {
                System.setProperty("user-service.dir", classes.getParent().getParent().toString());
            }
        } catch (URISyntaxException ignored) {
            // falls back to the working directory
        }
    }
}
