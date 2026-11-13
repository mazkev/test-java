package com.belajar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "seller_id") // Nama kolom foreign key di SQL
    private User seller;

    @NotBlank(message = "Nama produk tidak boleh kosong!")
    private String name;
    @NotNull(message = "Harga produk wajib diisi!")
    @Positive(message = "Harga produk harus lebih besar dari 0!")
    private Double price;
    @NotNull(message = "Stok produk wajib diisi!")
    @Min(value = 0, message = "Stok produk tidak boleh bernilai negatif!")
    private Integer stock;
    @NotBlank(message = "Kategori produk tidak boleh kosong!")
    private String category;

    public Product() {} // Constructor kosong wajib untuk JPA

    public Product(String name, Double price, Integer stock,String category) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    // Getter dan Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}