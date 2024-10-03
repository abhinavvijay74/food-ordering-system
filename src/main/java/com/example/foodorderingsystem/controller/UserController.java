package com.example.foodorderingsystem.controller;

import com.example.foodorderingsystem.dto.request.UserRequest;
import com.example.foodorderingsystem.exception.UserNotFoundException;
import com.example.foodorderingsystem.service.UserService;
import com.example.foodorderingsystem.utils.ResponseUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.foodorderingsystem.constants.SuccessConstants.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userServiceImpl;

    @Autowired
    public UserController(UserService userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    @PostMapping
    public ResponseEntity<Object> addUser(@Valid @RequestBody UserRequest userRequest) {
        try {
            userServiceImpl.addUser(userRequest);
            return ResponseUtils.successResponse(ADD_USER); // No content response
        } catch (Exception e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Object> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserRequest userRequest
    ) {
        try {
            userServiceImpl.updateUser(userId, userRequest);
            return ResponseUtils.successResponse(UPDATE_USER);
        } catch (UserNotFoundException e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.NOT_FOUND);
        }  catch (Exception e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable Long userId) {
        try {
            return  ResponseUtils.successResponse(
                    userServiceImpl.getUser(userId),
                    GET_USER,
                    SUCCESS
            );
        } catch (Exception e) {
            return ResponseUtils.exceptionResponse(e,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
