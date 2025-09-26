package com.example.studyapp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserUpdateConflictDto {
    private boolean emailExists;
    private boolean usernameExists;
}
