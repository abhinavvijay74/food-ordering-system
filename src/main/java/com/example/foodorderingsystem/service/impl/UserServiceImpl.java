package com.example.foodorderingsystem.service.impl;

import com.example.foodorderingsystem.dto.model.User;
import com.example.foodorderingsystem.dto.request.UserRequest;
import com.example.foodorderingsystem.exception.UserNotFoundException;
import com.example.foodorderingsystem.repository.UserRepository;
import com.example.foodorderingsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void addUser(UserRequest userRequest) {
        User user = User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .age(userRequest.getAge())
                .build();
        userRepository.save(user);
    }

    public void updateUser(Long userId, UserRequest userRequest) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setAge(userRequest.getAge());
        userRepository.save(user);
    }

    public User getUser(Long userId) throws UserNotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
