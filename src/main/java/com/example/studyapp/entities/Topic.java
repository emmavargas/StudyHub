package com.example.studyapp.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "topic_id")
    private List<FileAttachment> fileAttachments;


    public void addFileAttachments(List<FileAttachment> fileAttachments) {
        if (this.fileAttachments == null) {
            this.fileAttachments = new ArrayList<>();
        }
        this.fileAttachments.addAll(fileAttachments);
    }
}
