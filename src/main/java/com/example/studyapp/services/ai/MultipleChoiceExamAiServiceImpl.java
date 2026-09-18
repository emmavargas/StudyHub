package com.example.studyapp.services.ai;

import com.example.studyapp.dtos.CourseExamDto;
import com.example.studyapp.dtos.examChoice.ExamChoiceDto;
import com.example.studyapp.entities.Course;
import com.example.studyapp.entities.FileAttachment;
import com.example.studyapp.entities.Topic;
import com.example.studyapp.services.CourseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

@Primary
@Service
public class MultipleChoiceExamAiServiceImpl implements MultipleChoiceService {

    private final ResourceLoader resourceLoader;
    private final CourseService courseService;
    private final PdfTextExtractor pdfTextExtractor;

    public MultipleChoiceExamAiServiceImpl(CourseService courseService, ResourceLoader resourceLoader, PdfTextExtractor pdfTextExtractor) {
        this.courseService= courseService;
        this.resourceLoader = resourceLoader;

        this.pdfTextExtractor = pdfTextExtractor;
    }


    public ExamChoiceDto getMultipleChoice(Long id, CourseExamDto courseExamDto) {
        String json = generateChoiceExam(id, courseExamDto);
        json =json.replace("```json", "");
        json = json.replace("```", "");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ExamChoiceDto examChoiceDto = objectMapper.readValue(json, ExamChoiceDto.class);
            return examChoiceDto;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }


    public String generateChoiceExam(Long id, CourseExamDto courseExamDto) {
        String data = getDataCourse(id, courseExamDto);
        Resource resource = resourceLoader.getResource("classpath:prompts/system.txt");
        String content = "";
        try {
            content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo system.txt", e);
        }
        String fullPrompt = content + "\n" + data;

        try (Client client = new Client()) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(() -> {
                GenerateContentResponse response = client.models.generateContent(
                        "gemini-3.8-flash",
                        fullPrompt,
                        null);
                return response.text();
            });

            try {
                return future.get(20, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.out.println("Error baja conexion a internet");
                e.printStackTrace(); // Loguea el error en consola
                future.cancel(true);
                throw new RuntimeException("Error al generar el examen con la API de Gemini", e);
            } finally {
                executor.shutdownNow();
            }
        }
    }
    private String getDataCourse(Long id, CourseExamDto courseExamDto) {
        Course course = courseService.getCourse(id);
        List<Topic> topics = new ArrayList<>(course.getTopics()); // Crear una copia para no modificar la lista original
        String titleCourse = course.getTitle();

        // Desordenar los temas
        Collections.shuffle(topics);

        StringBuilder data = new StringBuilder();
        int i =1;
        for (Topic topic : topics) {
            for (FileAttachment fileAttachment : topic.getFileAttachments()) {
                String dataPdf = pdfTextExtractor.extractText(fileAttachment.getFilePath());
                data.append("Seccion: ").append(topic.getTitle());
                data.append(dataPdf).append("\n");
                data.append("\n");
                i++;
            }
        }

        return data.toString();
    }

}
