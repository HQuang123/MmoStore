package com.swp.mmostore.repository;

import com.swp.mmostore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    //This userRepository will help us to perform CRUD operations on User entity
    //It already includes normal methods like save (insert), findbyId. findAll, deletedById, count
    Optional<User> findByUsername(String username);
}
