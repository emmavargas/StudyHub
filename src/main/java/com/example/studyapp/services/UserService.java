package com.example.studyapp.services;

import com.example.studyapp.dtos.UserRegisterDto;
import com.example.studyapp.dtos.UserUpdatePasswordDto;
import com.example.studyapp.entities.Profile;
import com.example.studyapp.entities.User;
import com.example.studyapp.enums.Role;
import com.example.studyapp.exceptions.PasswordUpdateException;
import com.example.studyapp.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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




    @Transactional
    public User updatePassword(UserUpdatePasswordDto userUpdatePasswordDto) {

        User user = userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow();

        passwordEncoder.matches(userUpdatePasswordDto.getPassword(), user.getPassword());
        if (!passwordEncoder.matches(userUpdatePasswordDto.getPassword(), user.getPassword())) {
            throw new PasswordUpdateException("La contraseña actual es incorrecta","CURRENT_PASSWORD_INVALID");
        }
        if(!userUpdatePasswordDto.getNewPassword().equals(userUpdatePasswordDto.getConfirmNewPassword())) {
            throw new PasswordUpdateException("La nueva contraseña y la confirmación no coinciden","PASSWORD_CONFIRMATION_MISMATCH");
        }
        if (userUpdatePasswordDto.getPassword().equals(userUpdatePasswordDto.getNewPassword())) {
            throw new PasswordUpdateException("La nueva contraseña debe ser diferente a la actual","NEW_PASSWORD_SAME_AS_CURRENT");
        }
        user.setPassword(passwordEncoder.encode(userUpdatePasswordDto.getNewPassword()));
        return userRepository.save(user);
    }




    private String getUsernameContextSecurity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return authentication.getName();
    }
}
