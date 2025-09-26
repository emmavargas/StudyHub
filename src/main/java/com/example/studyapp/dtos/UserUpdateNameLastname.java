package com.example.studyapp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserUpdateNameLastname {

    private String name;
    private String lastname;
}
