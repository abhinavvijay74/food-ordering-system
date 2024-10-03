package com.example.foodorderingsystem.repository;

import com.example.foodorderingsystem.dto.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
