package com.example.foodorderingsystem.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RestaurantRequestDto {
    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotNull
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @DecimalMin(value = "0.0", inclusive = false, message = "Rating must be greater than 0")
    @DecimalMax(value = "5.0", message = "Rating must be less than or equal to 5")
    private BigDecimal rating;
}
