package com.belajar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Kode kupon tidak boleh kosong!")
    @Column(unique = true, nullable = false)
    private String code; // Contoh: "DISKON10", "HEMAT20RB"

    @NotNull(message = "Tipe diskon wajib ditentukan!")
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @NotNull(message = "Nilai diskon wajib diisi!")
    private Double discountValue; // 10.0 (jika persen) atau 20000.0 (jika fixed)

    private Double minPurchase = 0.0; // Minimal belanja agar kupon berlaku

    private Double maxDiscount; // Batas maksimal potongan untuk tipe persen (bisa null)

    private LocalDateTime expiryDate; // Tanggal kedaluwarsa

    private Integer usageLimit = 100; // Kuota berapa kali kupon bisa dipakai

    private Integer usedCount = 0; // Berapa kali sudah dipakai

    private Boolean active = true; // Status aktif / nonaktif

    public Coupon() {}

    public Coupon(String code, DiscountType discountType, Double discountValue, Double minPurchase, Double maxDiscount, LocalDateTime expiryDate, Integer usageLimit) {
        this.code = code != null ? code.toUpperCase() : null;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minPurchase = minPurchase != null ? minPurchase : 0.0;
        this.maxDiscount = maxDiscount;
        this.expiryDate = expiryDate;
        this.usageLimit = usageLimit != null ? usageLimit : 100;
        this.usedCount = 0;
        this.active = true;
    }

    // Helper: Menghitung berapa potongan harga yang didapat untuk total belanja tertentu
    public Double calculateDiscount(Double totalAmount) {
        if (!active || (expiryDate != null && LocalDateTime.now().isAfter(expiryDate))) {
            return 0.0;
        }
        if (totalAmount < minPurchase) {
            return 0.0;
        }

        double discount = 0.0;
        if (discountType == DiscountType.PERCENTAGE) {
            discount = totalAmount * (discountValue / 100.0);
            if (maxDiscount != null && discount > maxDiscount) {
                discount = maxDiscount;
            }
        } else if (discountType == DiscountType.FIXED_AMOUNT) {
            discount = Math.min(discountValue, totalAmount);
        }

        return discount;
    }

    // Getter dan Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code != null ? code.toUpperCase() : null; }

    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }

    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }

    public Double getMinPurchase() { return minPurchase; }
    public void setMinPurchase(Double minPurchase) { this.minPurchase = minPurchase; }

    public Double getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(Double maxDiscount) { this.maxDiscount = maxDiscount; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }

    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}