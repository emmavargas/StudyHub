package com.example.studyapp.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FileCleanupScheduler {

    private final CleanerService fileCleanupService;

    public FileCleanupScheduler(CleanerService fileCleanupService) {
        this.fileCleanupService = fileCleanupService;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void ejecutarLimpieza() {
        System.out.println("[CRON] Ejecutando limpieza de archivos... " + LocalDateTime.now());

        fileCleanupService.fileCleanup();
    }
}