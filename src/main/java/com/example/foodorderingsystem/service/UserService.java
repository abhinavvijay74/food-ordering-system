package com.example.foodorderingsystem.service;

import com.example.foodorderingsystem.dto.model.User;
import com.example.foodorderingsystem.dto.request.UserRequest;
import com.example.foodorderingsystem.exception.UserNotFoundException;

public interface UserService {
    void addUser(UserRequest userRequest);
    void updateUser(Long userId, UserRequest userRequest) throws UserNotFoundException;
    User getUser(Long userId) throws UserNotFoundException;
}
