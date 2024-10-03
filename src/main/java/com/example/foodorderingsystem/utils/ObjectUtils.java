package com.example.foodorderingsystem.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ObjectUtils {

    public static String convertObjectToJSON(Object object) {
        try {
            return new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            log.error("Error converting object = " + object + " to JSON. Failed with exception =" + ex.getMessage());
        }
        return "";
    }
}
