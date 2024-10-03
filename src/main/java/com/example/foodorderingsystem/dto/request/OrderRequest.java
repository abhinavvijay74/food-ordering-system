package com.example.foodorderingsystem.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Order items cannot be null")
    @Size(min = 1, message = "At least one order item is required")
    @Valid
    private List<OrderItemRequest> orderItems;
}
