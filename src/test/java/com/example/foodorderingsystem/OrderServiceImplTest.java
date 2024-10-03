package com.example.foodorderingsystem;

import com.example.foodorderingsystem.dto.model.*;
import com.example.foodorderingsystem.dto.request.OrderItemRequest;
import com.example.foodorderingsystem.dto.request.OrderRequest;
import com.example.foodorderingsystem.exception.*;
import com.example.foodorderingsystem.repository.MenuItemRepository;
import com.example.foodorderingsystem.repository.OrderRepository;
import com.example.foodorderingsystem.repository.RestaurantRepository;
import com.example.foodorderingsystem.service.impl.OrderServiceImpl;
import com.example.foodorderingsystem.service.impl.RestaurantServiceImpl;
import com.example.foodorderingsystem.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserServiceImpl userServiceImpl;

    @Mock
    private RestaurantServiceImpl restaurantServiceImpl;

    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    private OrderRequest validOrderRequest;
    private User mockUser;
    private Restaurant mockRestaurant1;
    private Restaurant mockRestaurant2;
    private MenuItem mockMenuItem1;
    private MenuItem mockMenuItem2;
    private Order mockOrder;
    private OrderItem mockOrderItem;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .userId(1L)
                .build();
        validOrderRequest = OrderRequest.builder()
                .userId(1L)
                .orderItems(Collections.singletonList(
                        OrderItemRequest.builder()
                                .itemName("Pizza")
                                .quantity(2) // Valid quantity
                                .build()))
                .build();
        mockRestaurant1 = Restaurant.builder()
                .id(1L)
                .capacity(10)
                .build();
        mockRestaurant2 = Restaurant.builder()
                .id(2L)
                .capacity(5)
                .build();

        mockMenuItem1 = MenuItem.builder()
                .id(1L)
                .name("Pizza")
                .price(10.00)
                .restaurant(mockRestaurant1)
                .build();
        mockMenuItem2 = MenuItem.builder()
                .id(2L)
                .name("Burger")
                .price(12.00)
                .restaurant(mockRestaurant2)
                .build();

        mockOrderItem = OrderItem.builder()
                .menuItem(mockMenuItem1)
                .quantity(2)
                .build();

        mockOrder = Order.builder()
                .orderId(1L)
                .user(mockUser)
                .amount(BigDecimal.valueOf(20.00))
                .orderStatus(OrderStatus.PLACED)
                .orderItems(Collections.singletonList(mockOrderItem))
                .build();
    }

    @Test
    void placeOrder_withValidOrder_savesOrderSuccessfully() throws Exception {

        when(userServiceImpl.getUser(1L)).thenReturn(mockUser);
        when(restaurantServiceImpl.findRestaurantsByItemNameWithSort("price", "Pizza",1))
                .thenReturn(Collections.singletonList(mockRestaurant1));
        when(menuItemRepository.findByNameAndRestaurantIdAndStatus("Pizza", 1L,Status.ACTIVE))
                .thenReturn(Optional.of(mockMenuItem1));
        lenient().when(restaurantRepository.findByIdWithPessimisticReadLock(1L))
                .thenReturn(Optional.of(mockRestaurant1));
        orderServiceImpl.placeOrder(validOrderRequest, "price");
        verify(orderRepository).save(any(Order.class));
        assertEquals(8, mockRestaurant1.getCapacity(),
                "Restaurant capacity should be reduced correctly");

    }

    @Test
    void placeOrder_restaurantCapacityNotSufficient_throwsOrderFulfillmentException() throws Exception {
        mockRestaurant1 = Restaurant.builder()
                .id(1L)
                .capacity(1)
                .build();
        when(userServiceImpl.getUser(1L)).thenReturn(mockUser);
        when(restaurantServiceImpl.findRestaurantsByItemNameWithSort("price", "Pizza",1))
                .thenReturn(Collections.singletonList(mockRestaurant1));
        when(menuItemRepository.findByNameAndRestaurantIdAndStatus("Pizza", 1L,Status.ACTIVE))
                .thenReturn(Optional.of(mockMenuItem1)); // Use the created mockMenuItem
        lenient().when(restaurantRepository.findByIdWithPessimisticReadLock(1L))
                .thenReturn(Optional.of(mockRestaurant1));
        assertThrows(OrderFulfillmentException.class, () -> {
            orderServiceImpl.placeOrder(validOrderRequest, "price");
        });
        // Verify that the orderRepository was never called
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_itemNotFound_throwsRestaurantNotFoundException() throws Exception {
        when(userServiceImpl.getUser(1L)).thenReturn(mockUser);
        when(restaurantServiceImpl.findRestaurantsByItemNameWithSort("price", "Pizza",1))
                .thenReturn(Collections.emptyList()); // No restaurants found

        // Act & Assert: Expecting a RestaurantNotFoundException when item is not found
        assertThrows(RestaurantNotFoundException.class, () -> {
            orderServiceImpl.placeOrder(validOrderRequest, "price");
        });

        // Verify that the orderRepository was never called
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_withMultipleRestaurants_savesOrdersSuccessfully() throws Exception {

        when(userServiceImpl.getUser(1L)).thenReturn(mockUser);
        when(restaurantServiceImpl.findRestaurantsByItemNameWithSort("price", "Pizza",1))
                .thenReturn(Collections.singletonList(mockRestaurant1)); // Only first restaurant for Pizza
        when(restaurantServiceImpl.findRestaurantsByItemNameWithSort("price", "Burger",1))
                .thenReturn(Collections.singletonList(mockRestaurant2)); // Only second restaurant for Burger

        when(menuItemRepository.findByNameAndRestaurantIdAndStatus("Pizza", 1L,Status.ACTIVE))
                .thenReturn(Optional.of(mockMenuItem1));
        when(menuItemRepository.findByNameAndRestaurantIdAndStatus("Burger", 2L,Status.ACTIVE))
                .thenReturn(Optional.of(mockMenuItem2));

        lenient().when(restaurantRepository.findByIdWithPessimisticReadLock(1L))
                .thenReturn(Optional.of(mockRestaurant1));
        lenient().when(restaurantRepository.findByIdWithPessimisticReadLock(2L))
                .thenReturn(Optional.of(mockRestaurant2));

        OrderRequest orderRequest = OrderRequest.builder()
                .userId(1L)
                .build();
        orderRequest.setOrderItems(Arrays.asList(
                new OrderItemRequest("Pizza", 2),
                new OrderItemRequest("Burger", 1)
        ));

        // Place the order
        orderServiceImpl.placeOrder(orderRequest, "price");

        // Verify that both orders are saved
        verify(orderRepository).save(any(Order.class));

        // Assert the restaurant capacities have been reduced correctly
        assertEquals(8, mockRestaurant1.getCapacity(),
                "Restaurant 1 capacity should be reduced correctly");
        assertEquals(4, mockRestaurant2.getCapacity(),
                "Restaurant 2 capacity should be reduced correctly");
    }

    @Test
    void completeOrder_withValidOrder_updatesOrderStatusAndReleasesCapacity() throws Exception {
        // Stubbing repository methods
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(menuItemRepository.existsById(mockMenuItem1.getId())).thenReturn(true);
        when(restaurantRepository.findByIdWithPessimisticReadLock(mockRestaurant1.getId()))
                .thenReturn(Optional.of(mockRestaurant1));

        // Call the completeOrder method
        orderServiceImpl.completeOrder(1L);

        // Verify that the order status was updated
        assertEquals(OrderStatus.COMPLETED, mockOrder.getOrderStatus(),
                "Order status should be updated to COMPLETED");

        // Verify that the restaurant capacity was released
        assertEquals(12, mockRestaurant1.getCapacity(),
                "Restaurant capacity should be increased correctly");

        // Verify that the order was saved
        verify(orderRepository).save(mockOrder);
    }

    @Test
    void completeOrder_orderNotFound_throwsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(OrderNotFoundException.class, () -> {
            orderServiceImpl.completeOrder(1L);
        });

        assertEquals("Order with id: 1 not found", exception.getMessage());
    }

    @Test
    void completeOrder_menuItemNotFound_throwsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(menuItemRepository.existsById(mockMenuItem1.getId())).thenReturn(false);

        Exception exception = assertThrows(MenuItemNotFoundException.class, () -> {
            orderServiceImpl.completeOrder(1L);
        });

        assertEquals("Menu item with id: 1 not found", exception.getMessage());
    }

    @Test
    void completeOrder_capacityReleaseFails_throwsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(menuItemRepository.existsById(mockMenuItem1.getId())).thenReturn(true);
        when(restaurantRepository.findByIdWithPessimisticReadLock(mockRestaurant1.getId()))
                .thenReturn(Optional.of(mockRestaurant1));

        // Simulate an optimistic locking failure
        doThrow(new OptimisticLockingFailureException("Optimistic Locking Failure"))
                .when(restaurantRepository).save(any(Restaurant.class));

        Exception exception = assertThrows(CapacityReleaseException.class, () -> {
            orderServiceImpl.completeOrder(1L);
        });
        assertEquals("Failed to release capacity for restaurant after multiple attempts.",
                exception.getMessage());
    }

}
