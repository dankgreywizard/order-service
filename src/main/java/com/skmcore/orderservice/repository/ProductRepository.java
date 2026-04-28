package com.skmcore.orderservice.repository;

import com.skmcore.orderservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByProductCode(String productCode);

    List<Product> findByAvailableTrue();

    List<Product> findByCategory(String category);

    List<Product> findByAvailableTrueAndCategory(String category);
}
