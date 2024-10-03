package com.example.foodorderingsystem.service.impl;

import com.example.foodorderingsystem.dto.model.Order;
import com.example.foodorderingsystem.dto.model.OrderItem;
import com.example.foodorderingsystem.dto.response.OrderDetailResponseDto;
import com.example.foodorderingsystem.exception.MenuItemNotFoundException;
import com.example.foodorderingsystem.exception.OrderNotFoundException;
import com.example.foodorderingsystem.exception.RestaurantNotFoundException;
import com.example.foodorderingsystem.repository.OrderRepository;
import com.example.foodorderingsystem.service.OrderHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderHistoryServiceImpl implements OrderHistoryService {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderHistoryServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderDetailResponseDto getOrderDetails(Long orderId) throws Exception {
        // Fetch order details along with associated order items
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        // Map order items to DTO
        // Fetch item name, throw exception if item not found
        // Fetch restaurant name, throw exception if restaurant not found
        List<OrderDetailResponseDto.OrderItemDetail> orderItemDetails = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            // Fetch item name, throw exception if item not found
            if (item.getMenuItem() == null) {
                throw new MenuItemNotFoundException(
                        "MenuItem not found for order item ID: " + item.getOrderItemId()
                );
            }

            // Fetch restaurant name, throw exception if restaurant not found
            if (item.getMenuItem().getRestaurant() == null) {
                throw new RestaurantNotFoundException(
                        "Restaurant not found for menu item ID: " + item.getMenuItem().getId()
                );
            }

            OrderDetailResponseDto.OrderItemDetail orderItemDetail = OrderDetailResponseDto.OrderItemDetail.builder()
                    .itemName(item.getMenuItem().getName())
                    .restaurantName(item.getMenuItem().getRestaurant().getName())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .createAt(item.getCreatedAt())
                    .updatedAt(item.getUpdatedAt())
                    .build();
            orderItemDetails.add(orderItemDetail);
        }

        // Build and return the OrderDetailResponseDto
        return OrderDetailResponseDto.builder()
                .userId(order.getUser().getUserId())
                .orderId(order.getOrderId())
                .amount(order.getAmount())
                .status(order.getOrderStatus())
                .createAt(order.getCreatedAt())
                .updateAt(order.getUpdatedAt())
                .orderItems(orderItemDetails)
                .build();
    }
}
