package com.example.studyapp.controllers;

import com.example.studyapp.dtos.CourseDto;
import com.example.studyapp.dtos.TopicDto;
import com.example.studyapp.entities.Course;
import com.example.studyapp.entities.FileAttachment;
import com.example.studyapp.entities.Topic;
import com.example.studyapp.repositories.FileAttachmentRepository;
import com.example.studyapp.services.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class CourseController {

    private final CourseService courseService;
    private final FileAttachmentRepository fileAttachmentRepository;
    private static final String UPLOAD_DIRECTORY = "/home/emma/data_study_hub/";

    //private static final String UPLOAD_DIRECTORY = "/home/emma/data_study_hub";


    public CourseController(CourseService courseService, FileAttachmentRepository fileAttachmentRepository) {
        this.courseService = courseService;
        this.fileAttachmentRepository = fileAttachmentRepository;
    }


    @PostMapping("/courses")
    public ResponseEntity<?> addCourse(@RequestBody CourseDto courseDto) {
        Course course = courseService.saveCourse(courseDto);
        return ResponseEntity.ok(course);
    }

    @GetMapping("/courses")
    public List<Course> showCourses() {
        return courseService.showCourses();
    }

    @PutMapping("/courses/{idCourse}")
    public ResponseEntity<?> updateCourse(@PathVariable Long idCourse, @RequestBody CourseDto courseDto) {
        try{
            Course course = courseService.updateCourse(courseDto,idCourse);
            return ResponseEntity.ok(course);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/courses/{idCourse}")
    public ResponseEntity<?> getCourse(@PathVariable Long idCourse) {
        try{
            Course course = courseService.getCourse(idCourse);
            return ResponseEntity.ok(course);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/courses/{idCourse}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long idCourse) {
        try{
            courseService.deleteCourse(idCourse);
            return ResponseEntity.ok("Ok");
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/courses/{idCourse}/topics")
    public ResponseEntity<?> addTopic(@PathVariable Long idCourse,
                                      @RequestPart("title") String title,
                                      @RequestPart("description") String description,
                                      @RequestPart("files") List<MultipartFile> files) {
        try{

            List<FileAttachment> attachments = files.stream().map(file -> {
                // Generar un nombre único con UUID para evitar duplicados
                String originalFileName = file.getOriginalFilename();
                String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;
                Path filePath = Paths.get(UPLOAD_DIRECTORY + uniqueFileName);

                // Guardar el archivo en la VPS
                try {
                    Files.createDirectories(filePath.getParent());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    Files.write(filePath, file.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // Crear entidad de archivo con la ruta
                FileAttachment attachment = new FileAttachment();
                attachment.setFileName(originalFileName);
                attachment.setFilePath(filePath.toString());
                attachment.setContentType(file.getContentType());
                return attachment;
            }).toList();

            TopicDto topicDto = new TopicDto(title,description);
            Topic topic = courseService.saveTopic(idCourse,topicDto, attachments);
            return ResponseEntity.ok(topic);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/courses/{idCourse}/topics/{idTopic}")
    public ResponseEntity<?> updateTopic(@PathVariable Long idTopic,@PathVariable Long idCourse,
                                         @RequestPart("title") String title,
                                         @RequestPart("description") String description,
                                         @RequestPart("files") List<MultipartFile> files) {
        try{

            List<FileAttachment> attachments = files.stream().map(file -> {
                // Generar un nombre único con UUID para evitar duplicados
                String originalFileName = file.getOriginalFilename();
                String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;
                Path filePath = Paths.get(UPLOAD_DIRECTORY + uniqueFileName);

                // Guardar el archivo en la VPS
                try {
                    Files.createDirectories(filePath.getParent());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    Files.write(filePath, file.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // Crear entidad de archivo con la ruta
                FileAttachment attachment = new FileAttachment();
                attachment.setFileName(originalFileName);
                attachment.setFilePath(filePath.toString());
                attachment.setContentType(file.getContentType());
                return attachment;
            }).toList();

            TopicDto topicDto = new TopicDto(title,description);
            Topic topic = courseService.updateTopic(idCourse,topicDto,idTopic, attachments);
            return ResponseEntity.ok(topic);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/courses/{idCourse}/topics/{idTopic}")
    public ResponseEntity<?> deleteTopic(@PathVariable Long idTopic,@PathVariable Long idCourse) {
        try{
            courseService.deleteTopic(idCourse,idTopic);
            return ResponseEntity.ok("Ok");
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/courses/{idCourse}/topics/{idTopic}/files/{idFile}")
    public ResponseEntity<?> deleteFile(@PathVariable Long idTopic,@PathVariable Long idCourse,@PathVariable Long idFile) {
        try{
            courseService.deleteFileTopic(idCourse,idTopic,idFile);
            return ResponseEntity.ok("Ok");
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/courses/{idCourse}/topics")
    public ResponseEntity<?> showTopics(@PathVariable Long idCourse) {
        try {
            List<Topic> topics = courseService.showTopics(idCourse);
            return ResponseEntity.ok(topics);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/courses/{idCourse}/topics/{idTopic}")
    public ResponseEntity<?> getTopic(@PathVariable Long idCourse, @PathVariable Long idTopic) {
        try {
            Topic topics = courseService.getTopic(idCourse,idTopic);
            return ResponseEntity.ok(topics);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/courses/files/{idFile}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long idFile) {
        FileAttachment file = fileAttachmentRepository.findById(idFile)
                .orElseThrow(() -> new IllegalArgumentException("Archivo no encontrado"));

        try {
            Path path = Paths.get(file.getFilePath());
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Archivo no accesible");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
