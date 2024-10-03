package com.example.foodorderingsystem.controller;

import com.example.foodorderingsystem.dto.PaginatedResponse;
import com.example.foodorderingsystem.dto.request.RestaurantRequestDto;
import com.example.foodorderingsystem.dto.response.RestaurantResponseDto;
import com.example.foodorderingsystem.exception.RestaurantAlreadyExistsException;
import com.example.foodorderingsystem.exception.RestaurantNotFoundException;
import com.example.foodorderingsystem.service.RestaurantService;
import com.example.foodorderingsystem.utils.ResponseUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import static com.example.foodorderingsystem.constants.SuccessConstants.*;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    private final RestaurantService restaurantServiceImpl;

    @Autowired
    public RestaurantController(RestaurantService restaurantServiceImpl) {
        this.restaurantServiceImpl = restaurantServiceImpl;
    }

    @PostMapping
    public ResponseEntity<Object> addRestaurant(@RequestBody @Valid RestaurantRequestDto restaurant) {
        try {
            restaurantServiceImpl.addRestaurant(restaurant);
            return ResponseUtils.successResponse(ADD_RESTAURANT);
        } catch (RestaurantAlreadyExistsException e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.CONFLICT);
        } catch (Exception e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateRestaurant(
            @PathVariable Long id,
            @RequestBody @Valid RestaurantRequestDto updatedRestaurant
    ) {
        try {
            restaurantServiceImpl.updateRestaurant(id, updatedRestaurant);
            return ResponseUtils.successResponse(UPDATE_RESTAURANT);
        } catch (RestaurantNotFoundException e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getRestaurantById(@PathVariable Long id) {
        try {
            RestaurantResponseDto restaurant = restaurantServiceImpl.getRestaurantById(id);
            return ResponseUtils.successResponse(
                    restaurant,
                    GET_RESTAURANT_BY_ID,
                    SUCCESS
            );
        } catch (RestaurantNotFoundException e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<Object> getAllRestaurants(
             @RequestParam(value = "page",defaultValue = "0") Integer page,
             @RequestParam(value = "size",defaultValue = "100") Integer size,
             @RequestParam(value = "itemName",required = false) String menuItemName
            ) {
        try {
            PaginatedResponse<RestaurantResponseDto> restaurants = restaurantServiceImpl.getAllRestaurants(
                    menuItemName,
                    page,
                    size
            );
            return ResponseUtils.successResponse(
                    restaurants,
                    GET_ALL_RESTAURANTS,
                    SUCCESS
            );
        } catch (Exception e) {
            return ResponseUtils.exceptionResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}