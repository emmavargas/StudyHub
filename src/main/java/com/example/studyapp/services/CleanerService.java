package com.example.studyapp.services;

import com.example.studyapp.entities.FileAttachment;
import com.example.studyapp.repositories.FileAttachmentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class CleanerService {

    private final FileAttachmentRepository fileRepository;

    public CleanerService(FileAttachmentRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Transactional
    public void fileCleanup() {
        List<FileAttachment> archivos = fileRepository.findBydeletePendingTrue();

        for (FileAttachment file : archivos) {
            Path path = Paths.get(file.getFilePath());
            try {
                if (Files.exists(path)) {
                    Files.delete(path);
                }
                fileRepository.delete(file);
            } catch (IOException e) {
                System.err.println("Error al borrar archivo: " + path);
            }
        }
    }
}
