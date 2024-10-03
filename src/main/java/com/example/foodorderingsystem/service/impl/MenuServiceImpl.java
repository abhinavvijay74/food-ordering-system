package com.example.foodorderingsystem.service.impl;

import com.example.foodorderingsystem.dto.model.MenuItem;
import com.example.foodorderingsystem.dto.model.Restaurant;
import com.example.foodorderingsystem.dto.model.Status;
import com.example.foodorderingsystem.dto.request.MenuItemRequest;
import com.example.foodorderingsystem.dto.response.MenuItemResponse;
import com.example.foodorderingsystem.exception.MenuItemAlreadyPresentException;
import com.example.foodorderingsystem.exception.MenuItemNotFoundException;
import com.example.foodorderingsystem.exception.RestaurantNotFoundException;
import com.example.foodorderingsystem.repository.MenuItemRepository;
import com.example.foodorderingsystem.repository.RestaurantRepository;
import com.example.foodorderingsystem.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import static com.example.foodorderingsystem.constants.ExceptionConstants.*;

@Service
public class MenuServiceImpl implements MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    @Autowired
    public MenuServiceImpl(
            MenuItemRepository menuItemRepository,
            RestaurantRepository restaurantRepository
    ) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public void addMenuItem(MenuItemRequest request) throws Exception {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(
                        String.format(
                                RESTAURANT_NOT_FOUND,request.getRestaurantId()
                        )
                ));

        boolean menuItemExists = menuItemRepository.existsByNameAndRestaurantId(
                request.getName(),
                restaurant.getId()
        );
        if (menuItemExists) {
            throw new MenuItemAlreadyPresentException(
                   String.format(MENU_ALREADY_EXISTS ,request.getName())
            );
        }

        MenuItem menuItem = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .status(request.getStatus())
                .restaurant(restaurant)
                .build();

        menuItemRepository.save(menuItem);
    }

    public void updateMenuItem(Long id, MenuItemRequest request) throws MenuItemNotFoundException
            ,RestaurantNotFoundException {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new MenuItemNotFoundException(
                        String.format(MENU_ITEM_NOT_FOUND,id)
                ));
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(
                        String.format(RESTAURANT_NOT_FOUND,request.getRestaurantId())
                ));
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setRestaurant(restaurant);
        menuItem.setStatus(request.getStatus());
        menuItemRepository.save(menuItem);
    }

    public List<MenuItemResponse> getAllMenuItemsByRestaurantId(Long restaurantId, Status status)
            throws RestaurantNotFoundException {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(
                        String.format(RESTAURANT_NOT_FOUND,restaurantId)
                ));
        List<MenuItem> menuItems = menuItemRepository.findByRestaurantIdAndStatus(restaurantId,status);
        return menuItems.stream()
                .map(menuItem -> MenuItemResponse.builder()
                        .id(menuItem.getId())
                        .name(menuItem.getName())
                        .description(menuItem.getDescription())
                        .price(menuItem.getPrice())
                        .createdAt(menuItem.getCreatedAt())
                        .updatedAt(menuItem.getUpdatedAt())
                        .build()).toList();
    }

    public List<String> getUniqueActiveMenuItemNames() {
        return menuItemRepository.findDistinctActiveMenuItemNames(Status.ACTIVE);
    }

    public MenuItemResponse getMenuById(Long id) throws Exception {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(()->new MenuItemNotFoundException(String.format(MENU_ITEM_NOT_FOUND,id)));
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .price(menuItem.getPrice())
                .updatedAt(menuItem.getUpdatedAt())
                .createdAt(menuItem.getCreatedAt())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .restaurantId(menuItem.getRestaurant().getId())
                .build();
    }
}
