package com.belajar.dto;

import com.belajar.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Nama tidak boleh kosong!")
    String name,

    @NotBlank(message = "Email tidak boleh kosong!")
    @Email(message = "Format email tidak valid!")
    String email,

    @NotBlank(message = "Password tidak boleh kosong!")
    @Size(min = 6, message = "Password minimal terdiri dari 6 karakter!")
    String password,

    Role role, // Boleh null (jika kosong, kita jadikan default: BUYER)

    String phoneNumber,
    String city,
    String address
) {}