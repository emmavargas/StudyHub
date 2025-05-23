package com.example.studyapp.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserResetPasswordDto {
    private String email;
    private String newPassword;
    private String confirmNewPassword;
}
