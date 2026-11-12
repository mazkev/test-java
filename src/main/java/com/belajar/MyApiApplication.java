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
    public CommandLineRunner initData(ProductRepository productRepository, com.belajar.repository.UserRepository userRepository) {
        return args -> {
            // 1. Buat Toko Penjual ID 1
            com.belajar.model.User tokoKopi = userRepository.save(
                new com.belajar.model.User("Toko Kopi Nusantara", "kopinusantara@mail.com", com.belajar.model.Role.SELLER)
            );

            // 2. Buat Produk yang terikat ke Toko Kopi Nusantara
            com.belajar.model.Product p1 = new com.belajar.model.Product("Kopi Gayo Arabika", 85000.0, 20);
            p1.setSeller(tokoKopi);
            productRepository.save(p1);

            com.belajar.model.Product p2 = new com.belajar.model.Product("Teh Hitam Walini", 25000.0, 50);
            p2.setSeller(tokoKopi);
            productRepository.save(p2);
        };
    }
}