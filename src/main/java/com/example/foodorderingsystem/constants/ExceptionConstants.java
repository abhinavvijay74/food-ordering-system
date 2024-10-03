package com.example.foodorderingsystem.constants;

public class ExceptionConstants {
    private ExceptionConstants() {

    }
    public static final String MENU_ALREADY_EXISTS = "Menu item with the name %s already exists for this restaurant";
    public static final String RESTAURANT_NOT_FOUND = "restaurant not found with id: %s";
    public static final String MENU_ITEM_NOT_FOUND = "Menu item with id: %s not found";
    public static final String UNABLE_TO_FULFILL_ITEM = "Unable to fully fulfill item: %s";
    public static final String NO_RESTAURANT_SERVES_ITEM = "No restaurant serves the item: %s";
    public static final String ORDER_NOT_FOUND = "Order with id: %s not found";
    public static final String CAPACITY_RELEASE_FAILED = "Failed to release capacity for restaurant after multiple attempts.";
    public static final String RESTAURANT_SELECTION_STRATEGY_NOT_FOUND = "No restaurant selection strategy found for: %s";
    public static final String MENU_ITEM_NOT_FOUND_WITH_NAME = "No menu items found with the name: %s";
    public static final String RESTAURANT_ALREADY_EXISTS = "Restaurant with this name already exists";
    public static final String RESTAURANT_UPDATE_FAILED = "Failed to update restaurant after multiple attempts due to version conflict";
    public static final String COMPLETE_ORDER_EXCEPTION = "Order %d is already completed.";
}
