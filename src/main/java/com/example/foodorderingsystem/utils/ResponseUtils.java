package com.example.foodorderingsystem.utils;

import com.example.foodorderingsystem.dto.Success;
import com.example.foodorderingsystem.dto.Error;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@UtilityClass
public class ResponseUtils {

    public static ResponseEntity<Object> successResponse(Object response, String message, String status) {
        return new ResponseEntity<>(new Success(response, message, status), HttpStatus.OK);
    }
    public static ResponseEntity<Object> successResponse(String message) {
        return new ResponseEntity<>(new Success(message), HttpStatus.OK);
    }

    public static ResponseEntity<Object> errorResponse(Error error, HttpStatus status) {
        log.error("Error: {}", ObjectUtils.convertObjectToJSON(error));
        return new ResponseEntity<>(error, status);
    }

    public static ResponseEntity<Object> exceptionResponse(Exception e, HttpStatus status) {
        List<String> errors = new ArrayList<>(1);
        errors.add(e.getMessage());
        log.error("Status : {}, Exception {}",status.toString(), e.getMessage());
        return errorResponse(new Error(e.getClass().getSimpleName(),errors),status);
    }

}

