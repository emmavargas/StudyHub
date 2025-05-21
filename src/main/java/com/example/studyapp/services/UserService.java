package com.example.studyapp.services;

import com.example.studyapp.dtos.UserRegisterDto;
import com.example.studyapp.entities.Profile;
import com.example.studyapp.entities.User;
import com.example.studyapp.enums.Role;
import com.example.studyapp.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User saveUser(UserRegisterDto userRegisterDto) {
        User user = new User();
        Profile profile = new Profile();
        profile.setName(userRegisterDto.getName());
        profile.setLastname(userRegisterDto.getLastname());
        user.setProfile(profile);
        user.setUsername(userRegisterDto.getUsername());
        user.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));
        user.setEmail(userRegisterDto.getEmail());
        user.setRole(Role.ROLE_USER);
        profile.setUser(user);
        return userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserId(Long userId) { return userRepository.findById(userId); }

}
