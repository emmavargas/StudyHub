package com.example.studyapp.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserUpdatePasswordDto {
    private String password;
    private String newPassword;
    private String confirmNewPassword;
}
