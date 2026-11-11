package com.belajar.service;

import com.belajar.model.Product;
import com.belajar.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.belajar.dto.CreateProductRequest;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    // Dependency Injection: Spring otomatis memasang ProductRepository ke Service
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(CreateProductRequest request) {
        Product newProduct = new Product(request.name(), request.price(), request.stock());
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