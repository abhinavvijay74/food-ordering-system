package com.example.foodorderingsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Success {
    private String message;
    private String status;
    private Object data;

    public Success(Object data) {
        this.data = data;
    }

    public Success(Object data, String message, String status) {
        this.message = message;
        this.status =status;
        this.data = data;
    }

    public Success(String message) {
        this.message = message;
    }

}