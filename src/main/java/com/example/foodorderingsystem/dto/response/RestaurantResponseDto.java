package com.example.foodorderingsystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RestaurantResponseDto {
    private Long id;
    private String name;
    private BigDecimal rating;
    private Integer capacity;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
