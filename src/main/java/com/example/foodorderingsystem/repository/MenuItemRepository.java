package com.example.foodorderingsystem.repository;

import com.example.foodorderingsystem.dto.model.MenuItem;
import com.example.foodorderingsystem.dto.model.Status;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantIdAndStatus(Long restaurantId,Status status);
    boolean existsByNameAndRestaurantId(String name, Long restaurantId);
    List<MenuItem> findByName(String name, PageRequest pageRequest);
    Optional<MenuItem> findByNameAndRestaurantIdAndStatus(String name, Long restaurantId, Status status);

    @Query("SELECT DISTINCT m.name FROM MenuItem m WHERE m.status = :status")
    List<String> findDistinctActiveMenuItemNames(@Param("status") Status status);
}
