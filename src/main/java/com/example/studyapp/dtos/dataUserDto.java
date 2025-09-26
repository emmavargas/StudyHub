package com.example.studyapp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class dataUserDto {
    private String email;
    private String username;
    private String name;
    private String lastname;
}
