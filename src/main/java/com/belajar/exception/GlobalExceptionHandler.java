package com.belajar.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

// @RestControllerAdvice adalah middleware global penangkap error di Spring Boot
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Menangkap IllegalArgumentException (Data tidak ditemukan -> 404 Not Found)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", 404,
            "error", "Not Found",
            "message", e.getMessage()
        ));
    }

    // Menangkap IllegalStateException (Validasi bisnis gagal / stok kurang -> 400 Bad Request)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleBadRequest(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", 400,
            "error", "Bad Request",
            "message", e.getMessage()
        ));
    }
        // Menangkap error validasi otomatis (@NotBlank, @Positive, @Email, @Min, dll)
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(org.springframework.web.bind.MethodArgumentNotValidException e) {
        java.util.Map<String, String> errors = new java.util.HashMap<>();
        
        // Kumpulkan semua field yang salah beserta pesan errornya
        for (var fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", 400,
            "error", "Validation Failed",
            "errors", errors
        ));
    }
        // Menangkap benturan Concurrency / Race Condition Flash Sale (409 Conflict)
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<?> handleOptimisticLockingFailure() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", 409,
            "error", "Conflict / Race Condition",
            "message", "Produk sedang diperebutkan oleh pembeli lain secara bersamaan. Silakan coba kembali!"
        ));
    }

}
