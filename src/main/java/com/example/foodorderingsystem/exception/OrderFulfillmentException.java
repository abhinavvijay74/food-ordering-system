package com.example.foodorderingsystem.exception;

public class OrderFulfillmentException extends RuntimeException {
    public OrderFulfillmentException(String message) {
        super(message);
    }

    public OrderFulfillmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
