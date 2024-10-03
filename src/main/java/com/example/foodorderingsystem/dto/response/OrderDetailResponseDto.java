package com.example.foodorderingsystem.dto.response;

import com.example.foodorderingsystem.dto.model.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderDetailResponseDto {
    private Long orderId;
    private BigDecimal amount;
    private Long userId;
    private OrderStatus status;
    private List<OrderItemDetail> orderItems;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    @Data
    @Builder
    public static class OrderItemDetail {
        private String itemName;
        private String restaurantName;
        private Integer quantity;
        private Double price;
        private LocalDateTime createAt;
        private LocalDateTime updatedAt;
    }
}
