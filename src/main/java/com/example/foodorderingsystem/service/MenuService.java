package com.example.foodorderingsystem.service;

import com.example.foodorderingsystem.dto.model.Status;
import com.example.foodorderingsystem.dto.request.MenuItemRequest;
import com.example.foodorderingsystem.dto.response.MenuItemResponse;
import com.example.foodorderingsystem.exception.MenuItemNotFoundException;
import com.example.foodorderingsystem.exception.RestaurantNotFoundException;

import java.util.List;

public interface MenuService {
    void addMenuItem(MenuItemRequest request) throws Exception;
    void updateMenuItem(Long id, MenuItemRequest request) throws MenuItemNotFoundException
            , RestaurantNotFoundException;
    List<MenuItemResponse> getAllMenuItemsByRestaurantId(Long restaurantId, Status status)
            throws RestaurantNotFoundException;
    List<String> getUniqueActiveMenuItemNames();
    MenuItemResponse getMenuById(Long id) throws Exception;
}
