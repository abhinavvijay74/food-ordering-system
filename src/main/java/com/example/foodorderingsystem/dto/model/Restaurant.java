package com.example.foodorderingsystem.dto.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "restaurants")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @Column(precision = 3, scale = 1) // DECIMAL(3, 1)
    private BigDecimal rating;

    @Version // For optimistic locking
    @Column(nullable = false)
    private Long version;

    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY)
    private Set<MenuItem> menuItems;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
