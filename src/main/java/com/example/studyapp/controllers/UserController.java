package com.example.studyapp.controllers;

import com.example.studyapp.dtos.*;
import com.example.studyapp.exceptions.PasswordUpdateException;
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
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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

            jwtCookie.setSecure(true);
            response.addCookie(jwtCookie);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("user", userDetails.getUsername());
            responseBody.put("message", "Login exitoso");
            return ResponseEntity.ok(responseBody);
        } catch (AuthenticationException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Usuario o contraseña incorrecta");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
            throw e;
        }
    }


    @PutMapping("/update-password")
    public ResponseEntity<?> updateUserPassword(@Valid @RequestBody UserUpdatePasswordDto userUpdatePasswordDto, BindingResult result) {
        if(result.hasErrors()) {
            return infoValidation(result);
        }
        try {
            userService.updatePassword(userUpdatePasswordDto);
            Map<String,String> dataSuccess = new HashMap<>();
            dataSuccess.put("message","Login exitoso");
            dataSuccess.put("code_error","LOGIN_SUCCESS");
            return ResponseEntity.ok(dataSuccess);
        }catch (PasswordUpdateException e) {
            Map<String,String> dataError = new HashMap<>();
            dataError.put("message", "Usuario o Email Existente");
            dataError.put("code_error",e.getErrorCode());
            return ResponseEntity.badRequest().body(dataError);
        }catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @PutMapping("/update-email-username")
    public ResponseEntity<?> updateEmailUsername(@RequestBody UserUpdateEmailUsername dto) {
        UserUpdateConflictDto conflicts = userService.checkCredentialConflictsEmailUsername(dto);

        if (conflicts.isEmailExists() || conflicts.isUsernameExists()) {
            return ResponseEntity.badRequest().body(conflicts);
        }

        userService.updateEmailUsername(dto);

        return ResponseEntity.ok(Collections.singletonMap("message", "Actualización exitosa"));
    }


    @PutMapping("/update-name-lastname")
    public ResponseEntity<?> updateNameLastname(@RequestBody UserUpdateNameLastname dto) {

        userService.updateNameLastname(dto);
        return ResponseEntity.ok(Collections.singletonMap("message", "Actualización exitosa"));
    }



    @GetMapping("/user/me")
    public ResponseEntity<dataUserDto> getCurrentUserEmailUsername() {
        dataUserDto dto = userService.getDataUser();
        return ResponseEntity.ok(dto);
    }


    private ResponseEntity<?> infoValidation(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }

}
