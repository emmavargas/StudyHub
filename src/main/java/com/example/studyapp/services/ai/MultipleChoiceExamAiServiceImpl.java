package com.example.studyapp.services.ai;

import com.example.studyapp.dtos.CourseExamDto;
import com.example.studyapp.dtos.examChoice.ExamChoiceDto;
import com.example.studyapp.entities.Course;
import com.example.studyapp.entities.Topic;
import com.example.studyapp.services.CourseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.*;

@Primary
@Service
public class MultipleChoiceExamAiServiceImpl implements MultipleChoiceService{

    private final ResourceLoader resourceLoader;
    private final CourseService courseService;

    public MultipleChoiceExamAiServiceImpl(CourseService courseService, ResourceLoader resourceLoader) {
        this.courseService= courseService;
        this.resourceLoader = resourceLoader;

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
        String content="";
        try {
            content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo system.txt", e);
        }
        String fullPrompt = content + "\n\n" + data;

        String projectId = "gen-lang-client-0183330281";
        String location = "us-central1";
        String modelName = "gemini-2.0-flash-001";

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {

            GenerationConfig config = GenerationConfig.newBuilder()
                    .setTemperature(0.7f)
                    .setMaxOutputTokens(3500)
                    .build();

            GenerativeModel model = new GenerativeModel.Builder()
                    .setModelName(modelName)
                    .setVertexAi(vertexAI)
                    .setGenerationConfig(config)
                    .setSystemInstruction(ContentMaker.fromString(content))
                    .build();

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(() -> {
                GenerateContentResponse response = model.generateContent(fullPrompt);
                return ResponseHandler.getText(response);
            });

            try {
                return future.get(10, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                throw new RuntimeException("Tiempo de espera agotado: el modelo tardó demasiado en responder.", e);
            } catch (Exception e) {
                throw new RuntimeException("Error al generar el examen con Vertex AI", e);
            } finally {
                executor.shutdownNow();
            }
        }
    }


    private String getDataCourse(Long id, CourseExamDto courseExamDto) {
        Course course = courseService.getCourse(id);
        String titleCourse = course.getTitle();
        List<Topic> topics = course.getTopics();

        String topicsX = String.join(",", courseExamDto.getTopics());

        List<Topic> topics2 = topics.stream().filter(topic ->
                topic.getTitle().contains(topicsX)
                ).toList();

        StringBuilder data = new StringBuilder();
        data.append("Titulo: ").append(titleCourse).append("\n");
        data.append("Bibliografia: ").append(course.getContentBibliography()).append("\n");
        data.append("Temas:").append("\n");
        for(Topic topic : topics2) {
           data.append(topic.getTitle()).append("\n");
           data.append(topic.getDescription()).append("\n");

            data.append(topic.getBibliography()).append("\n");

            data.append("\n");
        }
        return data.toString();
    }

}
