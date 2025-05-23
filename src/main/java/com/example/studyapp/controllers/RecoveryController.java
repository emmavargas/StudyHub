package com.example.studyapp.controllers;

import com.example.studyapp.dtos.UserResetPasswordDto;
import com.example.studyapp.exceptions.PasswordUpdateException;
import com.example.studyapp.exceptions.ResetPasswordException;
import com.example.studyapp.services.RecoveryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/recovery-password")
public class RecoveryController {

    private final RecoveryService recoveryService;

    public RecoveryController(RecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }

    @PostMapping("/send-email-code")
    public ResponseEntity<?> sendRecoveryCode(@RequestBody Map<String, String> body) {
        try {
            recoveryService.sendRecoveryCode(body.get("email"));
            Map<String, String> response = new HashMap<>();
            response.put("message", "Código enviado exitosamente");
            return ResponseEntity.ok(response);
        }catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email no encontrado");
            return ResponseEntity.badRequest().body(response);
        }
    }
    @PostMapping("/confirmation-code")
    public ResponseEntity<?> confimationCode(@RequestBody Map<String, String> body) {
        try {
            boolean isValid = recoveryService.validateCode(body.get("email"), body.get("code"));
            if(isValid){
                Map<String, String> response = new HashMap<>();
                response.put("message", "Codigo valido");
                return ResponseEntity.ok(response);
            }else {
                Map<String,String> map = new HashMap<>();
                map.put("message","El codigo no es valido");
                map.put("code_error","CODE_ERROR");
                return ResponseEntity.badRequest().body(map);
            }
        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (ResetPasswordException e) {
            Map<String,String> dataError = new HashMap<>();
            dataError.put("message",e.getMessage());
            dataError.put("code_error",e.getErrorCode());
            return ResponseEntity.badRequest().body(dataError);
        }
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody UserResetPasswordDto userResetPasswordDto) {
        try {
            recoveryService.resetPassword(userResetPasswordDto.getEmail(), userResetPasswordDto);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Constraseña cambiada exitosamente");
            return ResponseEntity.ok(response);
        }catch (PasswordUpdateException e) {
            Map<String,String> dataError = new HashMap<>();
            dataError.put("message",e.getMessage());
            dataError.put("code_error",e.getErrorCode());
            return ResponseEntity.badRequest().body(dataError);
        }catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
