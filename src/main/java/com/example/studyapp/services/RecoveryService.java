package com.example.studyapp.services;

import com.example.studyapp.dtos.UserResetPasswordDto;
import com.example.studyapp.entities.User;
import com.example.studyapp.exceptions.PasswordUpdateException;
import com.example.studyapp.exceptions.ResetPasswordException;
import com.example.studyapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RecoveryService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;
    public String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    @Transactional
    public void sendRecoveryCode(String email) {

        Optional<User> user = userRepository.findByEmail(email);
        if(user.isEmpty()) {
            throw new IllegalArgumentException("email not found");
        }
        String code = generateCode();
        String hashedCode = passwordEncoder.encode(code);
        user.get().setRecoveryNumber(hashedCode);
        user.get().setExpirationRecoveryNumber(LocalDateTime.now());
        userRepository.save(user.get());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("studyhubsistemanotificador@gmail.com");
        message.setTo(email);
        message.setSubject("Código de recuperación de cuenta");
        message.setText("Hola Usuario:" + user.get().getUsername() + "\n\n"+"Tu código de recuperación es: " + code + "\n\n" +
                "Este código expira en 15 minutos.");
        mailSender.send(message);
        System.out.println("Código enviado a: " + email);
    }

    @Transactional
    public boolean validateCode(String email, String code) {
        Optional<User> user = userRepository.findByEmail(email);
        LocalDateTime expirationDate = user.get().getExpirationRecoveryNumber().plusMinutes(15);
        if(user.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        if(expirationDate.isBefore(LocalDateTime.now())) {
            throw new ResetPasswordException("Code expired","CODE_EXPIRED");
        }

        String hashedCode = user.get().getRecoveryNumber();
        return passwordEncoder.matches(code, hashedCode);
    }

    @Transactional
    public void resetPassword(String email, UserResetPasswordDto userResetPasswordDto) {

        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOptional.get();

        String passwordEncoderNew = passwordEncoder.encode(userResetPasswordDto.getNewPassword());

        if(!userResetPasswordDto.getNewPassword().equals(userResetPasswordDto.getConfirmNewPassword())) {
            throw new PasswordUpdateException("La nueva contraseña y la confirmación no coinciden","PASSWORD_CONFIRMATION_MISMATCH");
        }
        if (user.getPassword().equals(passwordEncoderNew)) {
            throw new PasswordUpdateException("La nueva contraseña debe ser diferente a la actual","NEW_PASSWORD_SAME_AS_CURRENT");
        }
        user.setPassword(passwordEncoderNew);
        userRepository.save(user);
    }


}