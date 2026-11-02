package com.belajar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class MyApiApplication {

    public static void main(String[] args) {
        // Baris ini yang menyalakan server Spring Boot & Tomcat
        SpringApplication.run(MyApiApplication.class, args);
    }

    // Endpoint API pertama kita langsung di sini!
    @GetMapping("/")
    public String home() {
        return "Selamat datang! API ini dibangun dari nol.";
    }
}
