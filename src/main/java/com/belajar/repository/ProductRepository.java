package com.belajar.repository;

import com.belajar.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Cukup kosong seperti ini!
    // Spring otomatis menyediakan:
    // .findAll()       -> SELECT * FROM products
    // .findById(id)    -> SELECT * FROM products WHERE id = ?
    // .save(product)   -> INSERT atau UPDATE ke products
    // .deleteById(id)  -> DELETE FROM products WHERE id = ?
}
