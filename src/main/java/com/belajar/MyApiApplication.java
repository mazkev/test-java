package com.belajar;

import com.belajar.model.Product;
import com.belajar.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class MyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyApiApplication.class, args);
    }

    // Mesin pencampur/blender password standar industri (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initData(
            ProductRepository productRepository, 
            com.belajar.repository.UserRepository userRepository,
            com.belajar.repository.CouponRepository couponRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Password bawaan untuk testing: "password123" yang sudah di-hash BCrypt
            String defaultHashedPassword = passwordEncoder.encode("password123");

            // 1. Toko Penjual ID 1
            com.belajar.model.User tokoKopi = userRepository.save(
                new com.belajar.model.User(
                    "Toko Kopi Nusantara", 
                    "kopinusantara@mail.com", 
                    defaultHashedPassword,
                    com.belajar.model.Role.SELLER,
                    "081122334455",
                    "Banda Aceh",
                    "Jl. Teuku Umar No. 88, Banda Aceh"
                )
            );

            // 2. Pembeli ID 2 (Budi Santoso)
            userRepository.save(
                new com.belajar.model.User(
                    "Budi Santoso", 
                    "budi@mail.com", 
                    defaultHashedPassword,
                    com.belajar.model.Role.BUYER,
                    "081987654321",
                    "Jakarta Selatan",
                    "Jl. Sudirman Kav. 25, RT 01/RW 02, Jakarta Selatan"
                )
            );

            // 3. Produk awal
            com.belajar.model.Product p1 = new com.belajar.model.Product(
                "Kopi Gayo Arabika", 
                85000.0, 
                20, 
                "Minuman",
                "Kopi single origin Gayo Aceh 100% Arabika roast level medium. Cocok untuk manual brew V60 dan espresso.",
                "https://images.unsplash.com/photo-1559056199-641a0ac8b55e",
                "KOP-GAYO-250G",
                250
            );

             // 4. Seeder Kupon Promo Belanja
            couponRepository.save(new com.belajar.model.Coupon(
                "DISKON10",
                com.belajar.model.DiscountType.PERCENTAGE,
                10.0,
                50000.0,
                30000.0,
                java.time.LocalDateTime.now().plusMonths(1),
                100
            ));
            couponRepository.save(new com.belajar.model.Coupon(
                "HEMAT20RB",
                com.belajar.model.DiscountType.FIXED_AMOUNT,
                20000.0,
                70000.0,
                null,
                java.time.LocalDateTime.now().plusMonths(1),
                50
            ));
           
            
            p1.setSeller(tokoKopi);
            productRepository.save(p1);

            com.belajar.model.Product p2 = new com.belajar.model.Product(
                "Teh Hitam Walini", 
                25000.0, 
                50, 
                "Minuman",
                "Teh hitam murni khas perkebunan teh Jawa Barat, aroma wangi dan menyegarkan.",
                "https://images.unsplash.com/photo-1576092768241-dec231879fc3",
                "TEH-WALINI-100G",
                100
            );
            p2.setSeller(tokoKopi);
            productRepository.save(p2);
        };
    }
}