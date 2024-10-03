package com.example.foodorderingsystem.exception;

public class MenuItemAlreadyPresentException extends Exception {
    public MenuItemAlreadyPresentException(String message) {
        super(message);
    }
}
