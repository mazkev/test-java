package com.belajar.repository;

import com.belajar.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Otomatis mewarisi findAll(), findById(), save(), deleteById()
}