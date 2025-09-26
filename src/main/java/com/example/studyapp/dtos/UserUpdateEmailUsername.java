package com.example.studyapp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserUpdateEmailUsername {
    private String username;
    private String email;
}
