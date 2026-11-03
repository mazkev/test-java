package com.belajar.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long id;

    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String category;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    @Version
    private Long version;
    // Constructor kosong (Wajib untuk JPA)
    public Product() {}

    // Constructor lengkap
    public Product(String name, String description, Double price, Integer stock, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    // Getter dan Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }


}
