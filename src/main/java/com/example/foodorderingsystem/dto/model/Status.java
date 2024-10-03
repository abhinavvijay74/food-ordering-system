package com.example.foodorderingsystem.dto.model;

import lombok.Getter;

@Getter
public enum Status {
    ACTIVE("active"),
    INACTIVE("inactive");

    // Getter for the value
    private final String value;

    // Constructor
    Status(String value) {
        this.value = value;
    }

    // Static method to convert string to Status
    public static Status fromString(String status) {
        if (status != null) {
            for (Status s : Status.values()) {
                if (s.value.equalsIgnoreCase(status)) {
                    return s;
                }
            }
        }
        throw new IllegalArgumentException("No enum constant for status: " + status);
    }
}
