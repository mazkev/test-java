package com.belajar.service;

import com.belajar.model.Product;
import com.belajar.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.belajar.dto.CreateProductRequest;
import com.belajar.model.User;
import com.belajar.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // Dependency Injection: Spring otomatis memasang ProductRepository ke Service
    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Page<Product> getAllProducts(String name, String category, Pageable pageable) {
        if (name != null && !name.isBlank() && category != null && !category.isBlank()) {
            return productRepository.findByNameContainingIgnoreCaseAndCategoryIgnoreCase(name, category, pageable);
        } else if (name != null && !name.isBlank()) {
            return productRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (category != null && !category.isBlank()) {
            return productRepository.findByCategoryIgnoreCase(category, pageable);
        }
        return productRepository.findAll(pageable);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(CreateProductRequest request) {
        User seller = userRepository.findById(request.sellerId())
                .orElseThrow(() -> new IllegalArgumentException("Penjual dengan ID " + request.sellerId() + " tidak ditemukan!"));

        // Tambahkan request.category() di parameter ke-4:
        Product newProduct = new Product(request.name(), request.price(), request.stock(), request.category());
        newProduct.setSeller(seller);

        return productRepository.save(newProduct);
    }

    public Optional<Product> updateProduct(Long id, Product updatedProduct) {
        if (!productRepository.existsById(id)) {
            return Optional.empty();
        }
        updatedProduct.setId(id);
        return Optional.of(productRepository.save(updatedProduct));
    }

    public boolean deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            return false;
        }
        productRepository.deleteById(id);
        return true;
    }
}