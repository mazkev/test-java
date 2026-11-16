package com.belajar.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    // === FIELD BARU REAL MARKETPLACE ===
    private String phoneNumber; // Contoh: "081234567890"

    private String city; // Contoh: "Jakarta Selatan" atau "Banda Aceh"

    @Column(columnDefinition = "TEXT")
    private String address; // Alamat toko / alamat default pembeli

    public User() {}

    // Constructor lama (tetap dipertahankan supaya backward-compatible)
    public User(String name, String email, Role role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // Constructor baru lengkap
    public User(String name, String email, Role role, String phoneNumber, String city, String address) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.city = city;
        this.address = address;
    }

    // Getter dan Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}