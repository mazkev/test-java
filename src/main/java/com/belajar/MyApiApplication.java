package com.belajar;

import com.belajar.model.Product;
import com.belajar.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyApiApplication.class, args);
    }

    // Mengisi 2 data produk awal ke database saat server baru start
    @Bean
    public CommandLineRunner initData(ProductRepository repository) {
        return args -> {
            repository.save(new Product("Kopi Gayo Arabika", 85000.0, 20));
            repository.save(new Product("Teh Hitam Walini", 25000.0, 50));
        };
    }
}