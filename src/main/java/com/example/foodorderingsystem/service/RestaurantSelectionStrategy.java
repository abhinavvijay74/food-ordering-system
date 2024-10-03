package com.example.foodorderingsystem.service;

import com.example.foodorderingsystem.dto.model.Restaurant;

import java.util.List;

public interface RestaurantSelectionStrategy {
    List<Restaurant> findRestaurants(String itemName,Integer minCapacity);
}
