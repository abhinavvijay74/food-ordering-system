package com.example.foodorderingsystem.exception;

public class RestaurantNotFoundException extends Exception {
    public RestaurantNotFoundException(String message) {
        super(message);
    }
}