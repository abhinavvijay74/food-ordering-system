package com.example.foodorderingsystem;

import com.example.foodorderingsystem.dto.model.MenuItem;
import com.example.foodorderingsystem.dto.model.Restaurant;
import com.example.foodorderingsystem.dto.model.Status;
import com.example.foodorderingsystem.dto.request.MenuItemRequest;
import com.example.foodorderingsystem.dto.response.MenuItemResponse;
import com.example.foodorderingsystem.exception.MenuItemNotFoundException;
import com.example.foodorderingsystem.exception.RestaurantNotFoundException;
import com.example.foodorderingsystem.repository.MenuItemRepository;
import com.example.foodorderingsystem.repository.RestaurantRepository;
import com.example.foodorderingsystem.service.impl.MenuServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MenuServiceImplTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private MenuServiceImpl menuService;

    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurant = Restaurant.builder()
                .id(1L)
                .name("Pizza Place")
                .capacity(50)
                .rating(BigDecimal.valueOf(4.5))
                .build();
    }

    @Test
    void addMenuItem_ShouldSaveMenuItem_WhenRestaurantExists() throws Exception {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Margherita Pizza");
        request.setDescription("Classic cheese pizza");
        request.setPrice(8.99);
        request.setRestaurantId(1L);
        request.setStatus(Status.ACTIVE);

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        menuService.addMenuItem(request);

        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    @Test
    void addMenuItem_ShouldThrowRestaurantNotFoundException_WhenRestaurantDoesNotExist() {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Margherita Pizza");
        request.setDescription("Classic cheese pizza");
        request.setPrice(8.99);
        request.setRestaurantId(1L);
        request.setStatus(Status.ACTIVE);

        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class, () -> menuService.addMenuItem(request));
    }

    @Test
    void updateMenuItem_ShouldUpdateMenuItem_WhenExists() throws Exception {
        Long menuItemId = 1L;
        MenuItem existingMenuItem = MenuItem.builder()
                .id(menuItemId)
                .name("Old Pizza")
                .description("Old description")
                .price(10.0)
                .restaurant(restaurant)
                .status(Status.ACTIVE)
                .build();

        MenuItemRequest request = new MenuItemRequest();
        request.setName("Updated Pizza");
        request.setDescription("Updated description");
        request.setPrice(12.0);
        request.setRestaurantId(1L);

        when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(existingMenuItem));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        menuService.updateMenuItem(menuItemId, request);

        verify(menuItemRepository, times(1)).save(existingMenuItem);
        assert existingMenuItem.getName().equals("Updated Pizza");
        assert existingMenuItem.getDescription().equals("Updated description");
        assert existingMenuItem.getPrice() == 12.0;
    }

    @Test
    void updateMenuItem_ShouldThrowMenuItemNotFoundException_WhenMenuItemDoesNotExist() {
        Long menuItemId = 1L;
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Updated Pizza");
        request.setDescription("Updated description");
        request.setPrice(12.0);
        request.setRestaurantId(1L);

        when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.empty());

        assertThrows(MenuItemNotFoundException.class, () -> menuService.updateMenuItem(menuItemId, request));
    }

    @Test
    void getAllMenuItemsByRestaurantId_ShouldReturnMenuItems_WhenRestaurantExists() throws Exception {
        Long restaurantId = 1L;
        MenuItem menuItem = MenuItem.builder()
                .id(1L)
                .name("Margherita Pizza")
                .description("Classic cheese pizza")
                .price(8.99)
                .restaurant(restaurant)
                .status(Status.ACTIVE)
                .build();

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findByRestaurantIdAndStatus(restaurantId, Status.ACTIVE))
                .thenReturn(Collections.singletonList(menuItem));

        List<MenuItemResponse> menuItems = menuService.getAllMenuItemsByRestaurantId(restaurantId, Status.ACTIVE);

        assert menuItems.size() == 1;
        assert menuItems.get(0).getName().equals("Margherita Pizza");
    }

    @Test
    void getAllMenuItemsByRestaurantId_ShouldThrowRestaurantNotFoundException_WhenRestaurantDoesNotExist() {
        Long restaurantId = 1L;

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class, () -> menuService
                .getAllMenuItemsByRestaurantId(restaurantId, Status.ACTIVE));
    }

    @Test
    void testAddMenuItem_AlreadyExists() {
        // Arrange
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Pasta");
        request.setDescription("Delicious pasta");
        request.setPrice(10.0);
        request.setStatus(Status.ACTIVE);
        request.setRestaurantId(1L);

        when(restaurantRepository.findById(1L)).thenReturn(java.util.Optional.of(restaurant));
        when(menuItemRepository.existsByNameAndRestaurantId("Pasta", 1L)).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            menuService.addMenuItem(request);
        });

        String expectedMessage = "Menu item with the name Pasta already exists for this restaurant";
        assertEquals(expectedMessage,exception.getMessage());
    }
}
