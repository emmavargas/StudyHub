package com.example.studyapp.dtos;

import com.example.studyapp.entities.FileAttachment;
import com.example.studyapp.validations.IsExistsTopic;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TopicDto {
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Description is required")
    private String description;
    //private String Bibliography;

}
