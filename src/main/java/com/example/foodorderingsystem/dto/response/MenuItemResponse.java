package com.example.foodorderingsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Long restaurantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
