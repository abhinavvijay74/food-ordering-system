package com.example.foodorderingsystem.repository;

import com.example.foodorderingsystem.dto.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
