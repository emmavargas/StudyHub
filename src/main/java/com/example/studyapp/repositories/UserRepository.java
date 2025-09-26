package com.example.studyapp.repositories;

import com.example.studyapp.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    public Optional<User> findByUsername(String username);

    public boolean existsByUsername(String username);

    public Optional<User> findByEmail(String email);

}
