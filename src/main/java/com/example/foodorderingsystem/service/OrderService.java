package com.example.foodorderingsystem.service;

import com.example.foodorderingsystem.dto.request.OrderRequest;

public interface OrderService {
    void placeOrder(OrderRequest orderRequest, String sortBy) throws Exception;
    void completeOrder(Long orderId) throws Exception;
}
