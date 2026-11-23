package com.belajar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private Double totalAmount; // Total harga barang
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // === FIELD BARU REAL MARKETPLACE ===
    @Column(columnDefinition = "TEXT")
    private String shippingAddress;

    private String shippingCourier; // Contoh: "JNE Reguler"

    private Double shippingCost = 0.0; // Ongkir

    private Double grandTotal = 0.0; // totalAmount + shippingCost

    private String paymentMethod; // Contoh: "BCA_VA", "GOPAY", "COD"

    private String trackingNumber; // Nomor resi kurir (diisi saat status SHIPPED)

    private String couponCode; // Kode kupon yang dipakai (misal: "DISKON10")
    private Double discountAmount = 0.0; // Nominal potongan diskon kupon

    public Order() {}

    public Order(User buyer, String shippingAddress, String shippingCourier, Double shippingCost, String paymentMethod) {
        this.buyer = buyer;
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
        this.totalAmount = 0.0;
        this.shippingAddress = shippingAddress;
        this.shippingCourier = shippingCourier;
        this.shippingCost = shippingCost != null ? shippingCost : 0.0;
        this.grandTotal = 0.0;
        this.paymentMethod = paymentMethod;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        this.totalAmount += item.getSubtotal();
        this.grandTotal = this.totalAmount + this.shippingCost;
    }

    // Getter dan Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getShippingCourier() { return shippingCourier; }
    public void setShippingCourier(String shippingCourier) { this.shippingCourier = shippingCourier; }

    public Double getShippingCost() { return shippingCost; }
    public void setShippingCost(Double shippingCost) { this.shippingCost = shippingCost; }

    public Double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(Double grandTotal) { this.grandTotal = grandTotal; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
}