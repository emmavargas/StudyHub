package com.example.studyapp.services;

import com.example.studyapp.dtos.*;
import com.example.studyapp.entities.Profile;
import com.example.studyapp.entities.User;
import com.example.studyapp.enums.Role;
import com.example.studyapp.exceptions.PasswordUpdateException;
import com.example.studyapp.repositories.ProfileRepository;
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
    private final ProfileRepository profileRepository;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileRepository = profileRepository;
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

    @Transactional(readOnly = true)
    public User getUserEmail() {
        return userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow();
    }




    @Transactional
    public User updateEmailUsername(UserUpdateEmailUsername dto) {
        User user = userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow();

        // Validar si el nuevo username ya existe en otro usuario
        userRepository.findByUsername(dto.getUsername())
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(existingUser -> {
                    throw new IllegalArgumentException("El nombre de usuario ya está en uso");
                });

        // Validar si el nuevo email ya existe en otro usuario
        userRepository.findByEmail(dto.getEmail())
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(existingUser -> {
                    throw new IllegalArgumentException("El correo electrónico ya está en uso");
                });

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        return userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public UserUpdateConflictDto checkCredentialConflictsEmailUsername(UserUpdateEmailUsername dto) {
        User currentUser = userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow();

        boolean usernameExists = userRepository.findByUsername(dto.getUsername())
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .isPresent();

        boolean emailExists = userRepository.findByEmail(dto.getEmail())
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .isPresent();

        return new UserUpdateConflictDto(emailExists, usernameExists);
    }


    @Transactional
    public User updateNameLastname(UserUpdateNameLastname dto) {
        User currentUser = userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow();

        Profile profile = currentUser.getProfile();
        profile.setName(dto.getName());
        profile.setLastname(dto.getLastname());
        currentUser.setProfile(profile);
        return userRepository.save(currentUser);
    }


    @Transactional(readOnly = true)
    public dataUserDto getDataUser() {
        User user = userRepository.findByUsername(getUsernameContextSecurity())
                .orElseThrow(() -> new IllegalStateException("Usuario no autenticado"));

        Profile profile = user.getProfile();
        return new dataUserDto(user.getEmail(), user.getUsername(), profile.getName(), profile.getLastname());
    }


    private String getUsernameContextSecurity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return authentication.getName();
    }
}
