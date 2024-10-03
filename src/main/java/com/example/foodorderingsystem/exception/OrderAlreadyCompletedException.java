package com.example.foodorderingsystem.exception;

public class OrderAlreadyCompletedException extends Exception{
    public OrderAlreadyCompletedException(String message) {
        super(message);
    }
}
