package com.belajar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relasi: Banyak ulasan terhubung ke 1 Produk
    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonIgnore // Supaya tidak loop saat serialize JSON
    private Product product;

    // Relasi: Banyak ulasan dibuat oleh 1 Pembeli
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull(message = "Rating wajib diisi!")
    @Min(value = 1, message = "Rating minimal bintang 1!")
    @Max(value = 5, message = "Rating maksimal bintang 5!")
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private LocalDateTime createdAt;

    public Review() {}

    public Review(Product product, User user, Integer rating, String comment) {
        this.product = product;
        this.user = user;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    // Getter dan Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}