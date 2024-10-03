package com.example.foodorderingsystem.dto.request;

import com.example.foodorderingsystem.dto.model.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MenuItemRequest {
    @NotBlank(message = "Name is mandatory")
    private String name;

    private String description;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be greater than 0")
    private Double price;

    @NotNull
    private Status status;

    @NotNull(message = "Restaurant ID cannot be null")
    private Long restaurantId;
}
