package com.example.foodorderingsystem.controller;

import com.example.foodorderingsystem.dto.model.Status;
import com.example.foodorderingsystem.dto.request.MenuItemRequest;
import com.example.foodorderingsystem.exception.MenuItemNotFoundException;
import com.example.foodorderingsystem.exception.RestaurantNotFoundException;
import com.example.foodorderingsystem.service.MenuService;
import com.example.foodorderingsystem.utils.ResponseUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.foodorderingsystem.constants.SuccessConstants.*;

@RestController
@RequestMapping("/api/menu-items")
public class MenuController {
    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping
    public ResponseEntity<Object> addMenuItem(@RequestBody @Valid MenuItemRequest request) {
        try {
            menuService.addMenuItem(request);
            return ResponseUtils.successResponse(ADD_MENU_ITEM); // No content response
        } catch (RestaurantNotFoundException e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateMenuItem(
            @PathVariable Long id,
            @RequestBody @Valid MenuItemRequest request
    ) {
        try {
            menuService.updateMenuItem(id, request);
            return ResponseUtils.successResponse(UPDATE_MENU_ITEM);
        } catch (MenuItemNotFoundException | RestaurantNotFoundException e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.NOT_FOUND);
        }  catch (Exception e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getMenuById(@PathVariable Long id) {
        try {
            return ResponseUtils.successResponse(
                    menuService.getMenuById(id),
                    GET_MENU_BY_ID,
                    SUCCESS
            );
        } catch (MenuItemNotFoundException e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<Object> getAllMenuItemsByRestaurantId(
            @RequestParam(name = "restaurantId") Long restaurantId,
            @RequestParam(name = "status", defaultValue = "ACTIVE") String status
    ) {
        try {
            Status statusEnum = Status.fromString(status);
            return  ResponseUtils.successResponse(
                    menuService.getAllMenuItemsByRestaurantId(restaurantId,statusEnum),
                    GET_ALL_MENU,
                    SUCCESS
            );
        } catch (RestaurantNotFoundException e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/unique-names")
    public ResponseEntity<Object> getUniqueActiveMenuItemNames() {
        try {
            return ResponseUtils.successResponse(
                    menuService.getUniqueActiveMenuItemNames(),
                    GET_ALL_UNIQUE_MENU,
                    SUCCESS
            );
        } catch (Exception e) {
            return ResponseUtils.exceptionResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
