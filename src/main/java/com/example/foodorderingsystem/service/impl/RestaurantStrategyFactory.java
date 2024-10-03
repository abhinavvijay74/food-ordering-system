package com.example.foodorderingsystem.service.impl;

import com.example.foodorderingsystem.service.RestaurantSelectionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RestaurantStrategyFactory {
    private final Map<String, RestaurantSelectionStrategy> strategies = new HashMap<>();

    @Autowired
    public RestaurantStrategyFactory(List<RestaurantSelectionStrategy> strategyList) {
        for (RestaurantSelectionStrategy strategy : strategyList) {
            strategies.put(strategy.getClass().getAnnotation(Service.class).value(), strategy);
        }
    }

    public RestaurantSelectionStrategy getStrategy(String key) {
        return strategies.get(key);
    }
}
