package com.example.foodorderingsystem.service;

import com.example.foodorderingsystem.dto.PaginatedResponse;
import com.example.foodorderingsystem.dto.model.Restaurant;
import com.example.foodorderingsystem.dto.request.RestaurantRequestDto;
import com.example.foodorderingsystem.dto.response.RestaurantResponseDto;

import java.util.List;

public interface RestaurantService {
    void addRestaurant(RestaurantRequestDto restaurantRequestDto) throws Exception;
    void updateRestaurant(Long id, RestaurantRequestDto restaurantRequestDto) throws Exception;
    RestaurantResponseDto getRestaurantById(Long id) throws Exception;
    PaginatedResponse<RestaurantResponseDto> getRestaurantsByMenuItemName(
            String menuItemName,
            Integer page,
            Integer size
    );
    List<Restaurant> findRestaurantsByItemNameWithSort(String sortBy, String itemName, Integer minCapacity)
            throws Exception;
    PaginatedResponse<RestaurantResponseDto> getAllRestaurants(String menuItemName,Integer page,Integer size);

}
