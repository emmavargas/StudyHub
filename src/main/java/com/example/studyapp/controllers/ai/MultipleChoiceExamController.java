package com.example.studyapp.controllers.ai;

import com.example.studyapp.dtos.CourseExamDto;
import com.example.studyapp.dtos.examChoice.ExamChoiceDto;
import com.example.studyapp.entities.User;
import com.example.studyapp.services.CourseService;
import com.example.studyapp.services.UserService;
import com.example.studyapp.services.ai.MultipleChoiceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/user/courses/")
public class MultipleChoiceExamController {

    private final MultipleChoiceService multipleChoiceService;
    private final CourseService courseService;
    private final UserService userService;

    private final Map<Long, Bucket> buckets = new ConcurrentHashMap<>();
    public MultipleChoiceExamController(MultipleChoiceService multipleChoiceService, CourseService courseService, UserService userService) {
        this.multipleChoiceService= multipleChoiceService;
        this.courseService = courseService;
        this.userService = userService;
    }

    @PostMapping("/{id}/generar-examen")

    public ResponseEntity<?> generation(@RequestBody CourseExamDto courseExamDto, @PathVariable Long id) throws JsonProcessingException {
        Long userId = courseService.getUserId();
        User user = userService.getUserId(userId).get();
        if(user.isPremium()){
            Bucket bucket = resolveBucketPremium(userId);
            return getResponseEntity(courseExamDto, id, bucket);
        }
        Bucket bucket = resolveBucketFree(userId);
        return getResponseEntity(courseExamDto, id, bucket);

    }

    private ResponseEntity<?> getResponseEntity(@RequestBody CourseExamDto courseExamDto, @PathVariable Long id, Bucket bucket) throws JsonProcessingException {
        if(bucket.tryConsume(1)){
            try {
                ExamChoiceDto examChoiceDto = multipleChoiceService.getMultipleChoice(id, courseExamDto);
                return ResponseEntity.ok(examChoiceDto);
            }catch (RuntimeException e){
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(e.getMessage());
            }
        }else{
            Map<String,String> map = Map.of("message","Termino el limite de respuestas");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(map);
        }
    }

    private Bucket resolveBucketFree(Long userId) {
        return buckets.computeIfAbsent(userId, key -> {
            Bandwidth limit = Bandwidth.classic(4, Refill.intervally(4, Duration.ofMinutes(30)));
            return Bucket.builder().addLimit(limit).build();
        });
    }

    private Bucket resolveBucketPremium(Long userId) {
        return buckets.computeIfAbsent(userId, key -> {
            Bandwidth limit = Bandwidth.classic(50, Refill.intervally(50, Duration.ofMinutes(60)));
            return Bucket.builder().addLimit(limit).build();
        });
    }
}
