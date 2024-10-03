package com.example.foodorderingsystem.service.impl;

import com.example.foodorderingsystem.dto.model.Restaurant;
import com.example.foodorderingsystem.repository.RestaurantRepository;
import com.example.foodorderingsystem.service.RestaurantSelectionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service("price")
public class PriceBasedSelectionStrategy implements RestaurantSelectionStrategy {
    private final RestaurantRepository restaurantRepository;

    @Autowired
    public PriceBasedSelectionStrategy(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public List<Restaurant> findRestaurants(String itemName,Integer minCapacity) {
        return restaurantRepository.findRestaurantsByItemNameSortedByPrice(itemName,minCapacity);
    }
}
