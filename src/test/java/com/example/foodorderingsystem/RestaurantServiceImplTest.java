package com.example.foodorderingsystem;

import com.example.foodorderingsystem.dto.model.Restaurant;
import com.example.foodorderingsystem.dto.request.RestaurantRequestDto;
import com.example.foodorderingsystem.dto.response.RestaurantResponseDto;
import com.example.foodorderingsystem.exception.RestaurantAlreadyExistsException;
import com.example.foodorderingsystem.exception.RestaurantNotFoundException;
import com.example.foodorderingsystem.exception.SelectionStrategyNotFoundException;
import com.example.foodorderingsystem.repository.RestaurantRepository;
import com.example.foodorderingsystem.service.RestaurantSelectionStrategy;
import com.example.foodorderingsystem.service.impl.RestaurantServiceImpl;
import com.example.foodorderingsystem.service.impl.RestaurantStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestaurantServiceImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private RestaurantStrategyFactory restaurantStrategyFactory;
    @Mock
    private RestaurantSelectionStrategy restaurantSelectionStrategy;

    @InjectMocks
    private RestaurantServiceImpl restaurantServiceImpl;

    private RestaurantRequestDto restaurantRequestDto;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize sample data
        restaurantRequestDto = RestaurantRequestDto.builder()
                .name("Test Restaurant")
                .capacity(50)
                .rating(BigDecimal.valueOf(4.5))
                .build();

        restaurant = Restaurant.builder()
                .id(1L)
                .name("Test Restaurant")
                .capacity(50)
                .rating(BigDecimal.valueOf(4.5))
                .build();
    }

    @Test
    void testAddRestaurant_Success() throws Exception {
        when(restaurantRepository.existsByName(restaurantRequestDto.getName())).thenReturn(false);
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        restaurantServiceImpl.addRestaurant(restaurantRequestDto);

        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void testAddRestaurant_AlreadyExists() {
        when(restaurantRepository.existsByName(restaurantRequestDto.getName())).thenReturn(true);

        Exception exception = assertThrows(RestaurantAlreadyExistsException.class, () -> {
            restaurantServiceImpl.addRestaurant(restaurantRequestDto);
        });

        assertEquals("Restaurant with this name already exists", exception.getMessage());
        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }

    @Test
    void testUpdateRestaurant_Success() throws Exception {
        when(restaurantRepository. findByIdWithPessimisticReadLock(1L)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        restaurantServiceImpl.updateRestaurant(1L, restaurantRequestDto);

        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void testUpdateRestaurant_NotFound() {
        when(restaurantRepository. findByIdWithPessimisticReadLock(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RestaurantNotFoundException.class, () -> {
            restaurantServiceImpl.updateRestaurant(1L, restaurantRequestDto);
        });

        assertEquals("restaurant not found with id: 1", exception.getMessage());
        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }

    @Test
    void testGetRestaurantById_Success() throws Exception {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        RestaurantResponseDto responseDto = restaurantServiceImpl.getRestaurantById(1L);

        assertNotNull(responseDto);
        assertEquals("Test Restaurant", responseDto.getName());
        assertEquals(50, responseDto.getCapacity());
        assertEquals(BigDecimal.valueOf(4.5), responseDto.getRating());
    }

    @Test
    void testGetRestaurantById_NotFound() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RestaurantNotFoundException.class, () -> {
            restaurantServiceImpl.getRestaurantById(1L);
        });

        assertEquals("restaurant not found with id: 1", exception.getMessage());
    }

    @Test
    void testFindRestaurantsByItemNameWithSort_Success() throws Exception {
        String sortBy = "rating";
        String itemName = "Pizza";
        List<Restaurant> expectedRestaurants = Collections.singletonList(Restaurant.builder().build());

        when(restaurantStrategyFactory.getStrategy(sortBy.toLowerCase())).thenReturn(restaurantSelectionStrategy);
        when(restaurantSelectionStrategy.findRestaurants(itemName,1)).thenReturn(expectedRestaurants);

        List<Restaurant> actualRestaurants = restaurantServiceImpl.findRestaurantsByItemNameWithSort(
                sortBy,
                itemName,
                1
        );

        assertNotNull(actualRestaurants);
        assertEquals(expectedRestaurants.size(), actualRestaurants.size());
        verify(restaurantStrategyFactory, times(1)).getStrategy(sortBy.toLowerCase());
        verify(restaurantSelectionStrategy, times(1)).findRestaurants(itemName,1);
    }

    @Test
    void testFindRestaurantsByItemNameWithSort_StrategyNotFound() {
        String sortBy = "unknown";
        String itemName = "Pizza";

        when(restaurantStrategyFactory.getStrategy(sortBy.toLowerCase())).thenReturn(null);

        Exception exception = assertThrows(SelectionStrategyNotFoundException.class, () -> {
            restaurantServiceImpl.findRestaurantsByItemNameWithSort(sortBy, itemName,1);
        });

        assertEquals("No restaurant selection strategy found for: unknown", exception.getMessage());
        verify(restaurantStrategyFactory, times(1)).getStrategy(sortBy.toLowerCase());
        verify(restaurantSelectionStrategy, never()).findRestaurants(anyString(),anyInt());
    }

    @Test
    void testFindRestaurantsByItemNameWithSort_NoRestaurantsFound() {
        String sortBy = "rating";
        String itemName = "Pizza";

        when(restaurantStrategyFactory.getStrategy(sortBy.toLowerCase())).thenReturn(restaurantSelectionStrategy);
        when(restaurantSelectionStrategy.findRestaurants(itemName,1)).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(RestaurantNotFoundException.class, () -> {
            restaurantServiceImpl.findRestaurantsByItemNameWithSort(sortBy, itemName,1);
        });

        assertEquals("No restaurant serves the item: Pizza", exception.getMessage());
        verify(restaurantStrategyFactory, times(1)).getStrategy(sortBy.toLowerCase());
        verify(restaurantSelectionStrategy, times(1)).findRestaurants(itemName,1);
    }
}