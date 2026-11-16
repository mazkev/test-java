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
                        // 1. Buat Toko Penjual ID 1 (Lengkap dengan No HP, Kota Gudang, dan Alamat)
            com.belajar.model.User tokoKopi = userRepository.save(
                new com.belajar.model.User(
                    "Toko Kopi Nusantara", 
                    "kopinusantara@mail.com", 
                    com.belajar.model.Role.SELLER,
                    "081122334455",
                    "Banda Aceh",
                    "Jl. Teuku Umar No. 88, Banda Aceh"
                )
            );

            // Buat Pembeli ID 2 (Budi Santoso)
            userRepository.save(
                new com.belajar.model.User(
                    "Budi Santoso", 
                    "budi@mail.com", 
                    com.belajar.model.Role.BUYER,
                    "081987654321",
                    "Jakarta Selatan",
                    "Jl. Sudirman Kav. 25, RT 01/RW 02, Jakarta Selatan"
                )
            );


                        // 2. Buat Produk yang terikat ke Toko Kopi Nusantara
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