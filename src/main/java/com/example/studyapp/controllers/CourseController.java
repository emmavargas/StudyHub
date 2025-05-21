package com.example.studyapp.controllers;

import com.example.studyapp.dtos.CourseDto;
import com.example.studyapp.dtos.TopicDto;
import com.example.studyapp.entities.Course;
import com.example.studyapp.entities.Topic;
import com.example.studyapp.services.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
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
    public ResponseEntity<?> addTopic(@PathVariable Long idCourse, @RequestBody TopicDto topicDto) {
        try{
            Topic topic = courseService.saveTopic(idCourse,topicDto);
            return ResponseEntity.ok(topic);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/courses/{idCourse}/topics/{idTopic}")
    public ResponseEntity<?> updateTopic(@PathVariable Long idTopic, @RequestBody TopicDto topicDto,@PathVariable Long idCourse) {
        try{
            Topic topic = courseService.updateTopic(idCourse,topicDto,idTopic);
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

}
