package com.example.studyapp.services;


import com.example.studyapp.dtos.CourseDto;
import com.example.studyapp.dtos.TopicDto;
import com.example.studyapp.entities.Course;
import com.example.studyapp.entities.FileAttachment;
import com.example.studyapp.entities.Topic;
import com.example.studyapp.entities.User;
import com.example.studyapp.repositories.CourseRepository;
import com.example.studyapp.repositories.FileAttachmentRepository;
import com.example.studyapp.repositories.TopicRepository;
import com.example.studyapp.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final TopicRepository topicRepository;
    private final FileAttachmentRepository fileAttachmentRepository;


    public CourseService(UserRepository userRepository, CourseRepository courseRepository, TopicRepository topicRepository, FileAttachmentRepository fileAttachmentRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.topicRepository = topicRepository;
        this.fileAttachmentRepository = fileAttachmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Course> showCourses() {
        User user = userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow();
        System.out.println("Zona horaria del sistema: " + java.time.ZoneId.systemDefault());
        return user.getCourses();
    }

    @Transactional
    public Course saveCourse(CourseDto courseDto) {
        User user = userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow();
        Course course = new Course();
        course.setTitle(courseDto.getTitle());
        course.setContentBibliography(courseDto.getContentBibliography());
        user.getCourses().add(course);
        user = userRepository.save(user);
        return user.getCourses().get(user.getCourses().size() - 1);
    }

    @Transactional
    public Course updateCourse(CourseDto courseDto, Long idCourse) {
        User userDb = userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow();

        Course course = courseRepository.findById(idCourse).orElseThrow(
                () -> new IllegalArgumentException("Course not found")
        );

        if(!userDb.getCourses().contains(course)){
            throw new IllegalArgumentException("Course not found");
        }
        course.setTitle(courseDto.getTitle());
        course.setContentBibliography(courseDto.getContentBibliography());
        course = courseRepository.save(course);
        return course;
    }

    @Transactional(readOnly = true)
    public Course getCourse(Long idCourse) {

        User user = userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow();
        Course course = courseRepository.findById(idCourse).orElseThrow(
                () -> new IllegalArgumentException("Course not found")
        );
        if(!user.getCourses().contains(course)){
            throw new IllegalArgumentException("Course not found");
        }
        return course;
    }

    @Transactional
    public void deleteCourse(Long idCourse) {
        User user = userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow();
        Course course = courseRepository.findById(idCourse).orElseThrow(
                () -> new IllegalArgumentException("Course not found")
        );
        if(!user.getCourses().contains(course)){
            throw new IllegalArgumentException("Course not found");
        }

        List<Long> topicIds = course.getTopics().stream()
                .map(Topic::getId)
                .toList();

        if (!topicIds.isEmpty()) {
            // 2. Marcar archivos como pendientes de borrado
            fileAttachmentRepository.markAsPendingDeletionByTopicIds(topicIds);

            // 3. Desvincular los archivos (topic_id = null)
            fileAttachmentRepository.unlinkByTopicIds(topicIds);
        }
        user.getCourses().remove(course);
        userRepository.save(user);
    }

    @Transactional
    public Topic saveTopic(Long idCourse, TopicDto topicDto, List<FileAttachment> attachments) {

        User user = userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow();
        Course course = courseRepository.findById(idCourse).orElseThrow(
                () -> new IllegalArgumentException("Course not found")
        );
        if(!user.getCourses().contains(course)){
            throw new IllegalArgumentException("Course not found");
        }
        Topic topic = new Topic();
        topic.setTitle(topicDto.getTitle());
        topic.setDescription(topicDto.getDescription());
        topic.setFileAttachments(attachments);
        course.getTopics().add(topic);
        course = courseRepository.save(course);
        return course.getTopics().get(course.getTopics().size() - 1);
    }

    @Transactional
    public Topic updateTopic(Long idCourse, TopicDto topicDto, Long idTopic, List<FileAttachment> attachments) {

        User user = userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow();
        Course course = courseRepository.findById(idCourse).orElseThrow(
                () -> new IllegalArgumentException("Course not found")
        );
        if (!user.getCourses().contains(course)) {
            throw new IllegalArgumentException("Course not found");
        }
        Topic topic = topicRepository.findById(idTopic).orElseThrow(
                () -> new IllegalArgumentException("Topic not found")
        );
        topic.setTitle(topicDto.getTitle());
        topic.setDescription(topicDto.getDescription());
        topic.addFileAttachments(attachments);
        topic = topicRepository.save(topic);
        return topic;
    }

    //TENEMOS QUIE VERIFICAR COMO ELIMINAR

    @Transactional
    public void deleteTopic(Long idCourse, Long idTopic) {
        User user = userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow();
        Course course = courseRepository.findById(idCourse).orElseThrow(
                () -> new IllegalArgumentException("Course not found")
        );
        if(!user.getCourses().contains(course)){
            throw new IllegalArgumentException("Course not found");
        }
        Topic topic = topicRepository.findById(idTopic).orElseThrow(
                () -> new IllegalArgumentException("Topic not found")
        );

        fileAttachmentRepository.markAsPendingDeletionByTopicIds(List.of(idTopic));
        fileAttachmentRepository.unlinkByTopicIds(List.of(idTopic));


// Remover el topic de la lista de topics del curso
        course.getTopics().remove(topic);
        courseRepository.save(course);
        userRepository.save(user);
    }


    @Transactional
    public void deleteFileTopic(Long idCourse, Long idTopic, Long idFile) {
        User user = userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow();
        Course course = courseRepository.findById(idCourse).orElseThrow(
                () -> new IllegalArgumentException("Course not found")
        );

        if (!user.getCourses().contains(course)) {
            throw new IllegalArgumentException("Course not found");
        }

        Topic topic = topicRepository.findById(idTopic).orElseThrow(
                () -> new IllegalArgumentException("Topic not found")
        );

        FileAttachment fileToRemove = topic.getFileAttachments().stream()
                .filter(file -> file.getId().equals(idFile))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        // 1. Marcar y desvincular antes de remover
        fileAttachmentRepository.markAsPendingDeletionById(idFile);
        fileAttachmentRepository.unlinkById(idFile);
    }


    @Transactional(readOnly = true)
    public List<Topic> showTopics(Long idCourse) {

        User user = userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow();
        Course course = courseRepository.findById(idCourse).orElseThrow(
                () -> new IllegalArgumentException("Course not found")
        );
        if(!user.getCourses().contains(course)){
            throw new IllegalArgumentException("Course not found");
        }
        return course.getTopics();
    }

    @Transactional(readOnly = true)
    public Topic getTopic(Long idCourse, Long idTopic) {
        User user = userRepository.findByUsername(getUsernameContextSecurity()).orElseThrow();
        Course course = courseRepository.findById(idCourse).orElseThrow(
                () -> new IllegalArgumentException("Course not found")
        );
        if(!user.getCourses().contains(course)){
            throw new IllegalArgumentException("Course not found");
        }
        Topic topic = topicRepository.findById(idTopic).orElseThrow(
                () -> new IllegalArgumentException("Topic not found")
        );
        return topic;
    }

    @Transactional(readOnly = true)
    public boolean existsByTitleCourse(String title) {
        return courseRepository.existsByTitle(title);
    }

    @Transactional(readOnly = true)
    public boolean existsByTitleTopic(String title) {
        return topicRepository.existsByTitle(title);
    }


    private String getUsernameContextSecurity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return authentication.getName();
    }


    public Long getUserId() {
        String username = getUsernameContextSecurity();
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );
        return user.getId();
    }


}


