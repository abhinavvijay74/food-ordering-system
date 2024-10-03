package com.example.foodorderingsystem.service.impl;

import com.example.foodorderingsystem.dto.model.*;
import com.example.foodorderingsystem.dto.request.OrderItemRequest;
import com.example.foodorderingsystem.dto.request.OrderRequest;
import com.example.foodorderingsystem.exception.*;
import com.example.foodorderingsystem.repository.MenuItemRepository;
import com.example.foodorderingsystem.repository.OrderRepository;
import com.example.foodorderingsystem.repository.RestaurantRepository;
import com.example.foodorderingsystem.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static com.example.foodorderingsystem.constants.ExceptionConstants.*;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final UserServiceImpl userServiceImpl;
    private final RestaurantServiceImpl restaurantServiceImpl;

    @Autowired
    public OrderServiceImpl(
            MenuItemRepository menuItemRepository,
            RestaurantRepository restaurantRepository,
            OrderRepository orderRepository,
            UserServiceImpl userServiceImpl,
            RestaurantServiceImpl restaurantServiceImpl
    ) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
        this.userServiceImpl = userServiceImpl;
        this.restaurantServiceImpl = restaurantServiceImpl;
    }

    private static final int MAX_RETRIES = 3;

    @Transactional(rollbackFor = Exception.class)
    public void placeOrder(OrderRequest orderRequest,String sortBy) throws Exception{
        // Create an empty order
        User user = userServiceImpl.getUser(orderRequest.getUserId());
        Order order = Order.builder()
                .user(user)
                .amount(BigDecimal.ZERO)
                .orderStatus(OrderStatus.PLACED)
                .orderItems(new ArrayList<>())
                .build();

        // Process each order item request individually
        processOrderItems(orderRequest, order, sortBy);

        // Calculate total amount and save the order
        order.setAmount(calculateTotalAmount(order));
        orderRepository.save(order);
    }

    // Main method to process all order items
    private void processOrderItems(OrderRequest orderRequest, Order order, String sortBy) throws Exception {
        for (OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
            List<Restaurant> restaurants = restaurantServiceImpl.findRestaurantsByItemNameWithSort(
                    sortBy, itemRequest.getItemName(),1
            );
            if (restaurants.isEmpty()) {
                throw new RestaurantNotFoundException(
                        String.format(NO_RESTAURANT_SERVES_ITEM, itemRequest.getItemName())
                );
            }

            int totalQuantityFulfilled = fulfillOrderItem(order, itemRequest, restaurants);

            if (totalQuantityFulfilled < itemRequest.getQuantity()) {
                throw new OrderFulfillmentException(
                        String.format(UNABLE_TO_FULFILL_ITEM, itemRequest.getItemName())
                );
            }
        }
    }

    // Helper method to fulfill a single order item
    private int fulfillOrderItem(Order order, OrderItemRequest itemRequest, List<Restaurant> restaurants)
            throws Exception {
        int quantityNeeded = itemRequest.getQuantity();
        int totalQuantityFulfilled = 0;

        for (Restaurant restaurant : restaurants) {
            if (quantityNeeded <= 0) break;

            int availableQuantity = 0;
            try {
                availableQuantity = checkAndReserveCapacityWithRetry(restaurant, quantityNeeded);
            } catch (Exception e) {
                log.error("Exception occurred in calling checkAndReserveCapacityWithRetry", e);
            }

            MenuItem menuItem = menuItemRepository.findByNameAndRestaurantIdAndStatus(
                    itemRequest.getItemName(), restaurant.getId(),Status.ACTIVE).orElseThrow(
                    () -> new MenuItemNotFoundException("Requested Item Not Found")
            );

            if (availableQuantity > 0) {
                OrderItem orderItem = OrderItem.builder()
                        .menuItem(menuItem)
                        .order(order)
                        .price(menuItem.getPrice())
                        .quantity(availableQuantity)
                        .build();
                order.getOrderItems().add(orderItem);

                quantityNeeded -= availableQuantity;
                totalQuantityFulfilled += availableQuantity;
            }
        }

        return totalQuantityFulfilled;
    }


    // Reserve capacity for a restaurant with retry logic
    private int checkAndReserveCapacityWithRetry(Restaurant restaurant, int quantityNeeded)
            throws RestaurantNotFoundException, InterruptedException {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                return reserveCapacity(restaurant, quantityNeeded);
            } catch (ObjectOptimisticLockingFailureException e) {
                retryCount++;
                Thread.sleep(5);
                restaurant = restaurantRepository.findByIdWithPessimisticReadLock(restaurant.getId()).orElseThrow(() ->
                        new RestaurantNotFoundException("Restaurant not found"));
            }
        }
        return 0;
    }

    // Reserve capacity and ensure it is part of the transaction
    private int reserveCapacity(Restaurant restaurant, int quantityNeeded) {
        int currentCapacity = restaurant.getCapacity();
        if (currentCapacity >= quantityNeeded) {
            restaurant.setCapacity(currentCapacity - quantityNeeded);
            restaurantRepository.save(restaurant);
            return quantityNeeded;
        } else if (currentCapacity > 0) {
            restaurant.setCapacity(0);
            restaurantRepository.save(restaurant);
            return currentCapacity;
        }
        return 0;
    }

    // Method to calculate total amount
    private BigDecimal calculateTotalAmount(Order order) {
        double total = 0.0;
        for (OrderItem orderItem : order.getOrderItems()) {
            double itemPrice = orderItem.getMenuItem().getPrice();
            total += itemPrice * orderItem.getQuantity();
        }
        return BigDecimal.valueOf(total);
    }

    // Method to complete an order
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(String.format(ORDER_NOT_FOUND, orderId)));

        // Check if the order is already completed
        if (OrderStatus.COMPLETED.equals(order.getOrderStatus())) {
            throw new OrderAlreadyCompletedException(String.format(COMPLETE_ORDER_EXCEPTION, orderId));
        }

        // Change the order status to COMPLETED
        order.setOrderStatus(OrderStatus.COMPLETED);

        // Release restaurant capacities with retry logic
        for (OrderItem orderItem : order.getOrderItems()) {
            Long menuItemId = orderItem.getMenuItem().getId();
            if (!menuItemRepository.existsById(menuItemId)) {
                throw new MenuItemNotFoundException(String.format(MENU_ITEM_NOT_FOUND,menuItemId));
            }
            Long restaurantId = orderItem.getMenuItem().getRestaurant().getId();
            releaseCapacityWithRetry(restaurantId, orderItem.getQuantity());
        }

        // Save the updated order
        orderRepository.save(order);
    }

    // Method to release capacity for a restaurant with retry logic
    private void releaseCapacityWithRetry(Long restaurantId, int quantity) throws Exception {
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                // Fetch the restaurant
                Restaurant restaurant = restaurantRepository.findByIdWithPessimisticReadLock(restaurantId)
                        .orElseThrow(
                                () -> new RestaurantNotFoundException(
                                        String.format(RESTAURANT_NOT_FOUND,restaurantId)
                                )
                        );

                // Release capacity for the restaurant
                restaurant.setCapacity(restaurant.getCapacity() + quantity);
                restaurantRepository.save(restaurant);
                return; // Exit if the capacity release was successful

            } catch (OptimisticLockingFailureException e) {
                retryCount++;
                Thread.sleep(5);
            }
        }
        throw new CapacityReleaseException(
                CAPACITY_RELEASE_FAILED
        );
    }
}
