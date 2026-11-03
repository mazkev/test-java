package com.belajar.repository;

import com.belajar.model.Role;
import com.belajar.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role); // Mencari semua toko (SELLER) atau semua pembeli (BUYER)
}
