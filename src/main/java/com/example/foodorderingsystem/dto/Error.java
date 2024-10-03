package com.example.foodorderingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class Error {
    private String message;
    private List<String> errors;

    public Error(String message, List<String> errors) {
        this.message = message;
        this.errors = errors;
    }
}
