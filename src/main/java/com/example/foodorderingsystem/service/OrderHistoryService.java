package com.example.foodorderingsystem.service;

import com.example.foodorderingsystem.dto.response.OrderDetailResponseDto;

public interface OrderHistoryService {
    OrderDetailResponseDto getOrderDetails(Long orderId) throws Exception;
}
