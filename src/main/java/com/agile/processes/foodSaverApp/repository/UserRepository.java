package com.agile.processes.foodSaverApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agile.processes.foodSaverApp.entities.User;

public interface UserRepository extends JpaRepository<User,Long> {
    public User findByEmailAndPassword(String email,String password);
}
