package com.agile.processes.foodSaverApp.repository;

import com.agile.processes.foodSaverApp.entities.NGO;
import com.agile.processes.foodSaverApp.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NGORepository extends JpaRepository<NGO, Long> {
    boolean existsByUser(User user);
    Optional<NGO> findByUser(User user);
}
