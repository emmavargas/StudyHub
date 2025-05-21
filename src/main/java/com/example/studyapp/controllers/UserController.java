package com.example.studyapp.controllers;

import com.example.studyapp.dtos.UserRegisterDto;
import com.example.studyapp.dtos.UserLoginDto;
import com.example.studyapp.security.JpaUserDetailsService;
import com.example.studyapp.security.JwtUtils;
import com.example.studyapp.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JpaUserDetailsService jpaUserDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegisterDto userRegisterDto, BindingResult result) {

        if (result.hasErrors()) {
            return infoValidation(result);
        }

        userService.saveUser(userRegisterDto);
        Map<String, Object> response = new HashMap<>();
        response.put("success",true);
        response.put("message", "Usuario registrado con exito");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUsername(@Valid @RequestBody UserLoginDto userLoginDto, BindingResult result, HttpServletResponse response) {
        if (result.hasErrors()) {
            return infoValidation(result);
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLoginDto.getUsername(), userLoginDto.getPassword()));
            UserDetails userDetails = jpaUserDetailsService.loadUserByUsername(userLoginDto.getUsername());
            String token = jwtUtils.generateToken(userDetails);
//            Aqui estaba el token enviado, modificamos por una cookie
//            Map<String, Object> response = new HashMap<>();
//            response.put("token", "Bearer " + token);
//            response.put("user", userDetails.getUsername());
//            response.put("message", "Login existosoooo");


            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60);

            jwtCookie.setSecure(false);
            response.addCookie(jwtCookie);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("user", userDetails.getUsername());
            responseBody.put("message", "Login exitoso");
            return ResponseEntity.ok(responseBody);
        } catch (AuthenticationException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Credenciales inválidas");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
            throw e;
        }
    }


    private ResponseEntity<?> infoValidation(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }

}
