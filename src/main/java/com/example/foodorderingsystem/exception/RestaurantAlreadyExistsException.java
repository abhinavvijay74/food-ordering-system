package com.example.foodorderingsystem.exception;

public class RestaurantAlreadyExistsException extends Exception {
    public RestaurantAlreadyExistsException(String message) {
        super(message);
    }
}