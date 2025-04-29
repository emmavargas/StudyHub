package com.example.studyapp.services.ai;

import com.example.studyapp.dtos.CourseExamDto;
import com.example.studyapp.dtos.examChoice.ExamChoiceDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

//@Primary
@Service
public class MultipleChoiceExamStaticImpl implements MultipleChoiceService{

    @Override
    public ExamChoiceDto getMultipleChoice(Long id, CourseExamDto courseExamDto) {
        String json = "{\n" +
                "    \"course\": \"Analisis Matematico II\",\n" +
                "    \"topics\": [\n" +
                "        \"Logica\"\n" +
                "    ],\n" +
                "    \"items\": [\n" +
                "        {\n" +
                "            \"question\": \"¿Cuál de las siguientes opciones representa la negación de la proposición 'Todos los números pares son divisibles por 2'?\",\n" +
                "            \"options\": [\n" +
                "                \"Ningún número par es divisible por 2.\",\n" +
                "                \"Algunos números pares no son divisibles por 2.\",\n" +
                "                \"Todos los números impares son divisibles por 2.\",\n" +
                "                \"Algunos números pares son divisibles por 2.\"\n" +
                "            ],\n" +
                "            \"answer\": \"Algunos números pares no son divisibles por 2.\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"question\": \"Si p es verdadero y q es falso, ¿cuál es el valor de verdad de la proposición compuesta (p ∧ q) → ¬q?\",\n" +
                "            \"options\": [\n" +
                "                \"Verdadero\",\n" +
                "                \"Falso\",\n" +
                "                \"No se puede determinar\",\n" +
                "                \"Depende del contexto\"\n" +
                "            ],\n" +
                "            \"answer\": \"Verdadero\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"question\": \"¿Cuál de las siguientes leyes lógicas permite simplificar la expresión (A ∨ A)?\",\n" +
                "            \"options\": [\n" +
                "                \"Ley de la doble negación\",\n" +
                "                \"Ley de idempotencia\",\n" +
                "                \"Ley de absorción\",\n" +
                "                \"Ley de De Morgan\"\n" +
                "            ],\n" +
                "            \"answer\": \"Ley de idempotencia\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"question\": \"La expresión 'Si llueve, entonces la calle se moja' es equivalente a:\",\n" +
                "            \"options\": [\n" +
                "                \"Llueve si y solo si la calle se moja.\",\n" +
                "                \"Si la calle se moja, entonces llueve.\",\n" +
                "                \"No llueve o la calle se moja.\",\n" +
                "                \"Si no llueve, entonces la calle no se moja.\"\n" +
                "            ],\n" +
                "            \"answer\": \"No llueve o la calle se moja.\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"question\": \"¿Qué tipo de razonamiento se utiliza cuando se llega a una conclusión general a partir de observaciones específicas?\",\n" +
                "            \"options\": [\n" +
                "                \"Deductivo\",\n" +
                "                \"Inductivo\",\n" +
                "                \"Abductivo\",\n" +
                "                \"Analógico\"\n" +
                "            ],\n" +
                "            \"answer\": \"Inductivo\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"question\": \"¿Cuál de las siguientes expresiones representa la ley de De Morgan para la negación de una conjunción?\",\n" +
                "            \"options\": [\n" +
                "                \"¬(p ∧ q) ≡ ¬p ∧ ¬q\",\n" +
                "                \"¬(p ∧ q) ≡ ¬p ∨ ¬q\",\n" +
                "                \"¬(p ∨ q) ≡ ¬p ∨ ¬q\",\n" +
                "                \"¬(p ∨ q) ≡ ¬p ∧ ¬q\"\n" +
                "            ],\n" +
                "            \"answer\": \"¬(p ∧ q) ≡ ¬p ∨ ¬q\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"question\": \"En lógica proposicional, ¿qué significa que una proposición sea una tautología?\",\n" +
                "            \"options\": [\n" +
                "                \"Que siempre es falsa.\",\n" +
                "                \"Que siempre es verdadera.\",\n" +
                "                \"Que puede ser verdadera o falsa dependiendo del contexto.\",\n" +
                "                \"Que no tiene valor de verdad definido.\"\n" +
                "            ],\n" +
                "            \"answer\": \"Que siempre es verdadera.\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"question\": \"¿Cuál de las siguientes inferencias es un ejemplo de Modus Ponens?\",\n" +
                "            \"options\": [\n" +
                "                \"Si p → q y ¬q, entonces ¬p.\",\n" +
                "                \"Si p → q y p, entonces q.\",\n" +
                "                \"Si ¬p → ¬q y q, entonces p.\",\n" +
                "                \"Si p ∨ q y ¬p, entonces q.\"\n" +
                "            ],\n" +
                "            \"answer\": \"Si p → q y p, entonces q.\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"question\": \"Si una proposición y su negación son ambas verdaderas, ¿qué principio lógico se viola?\",\n" +
                "            \"options\": [\n" +
                "                \"Principio de identidad\",\n" +
                "                \"Principio de no contradicción\",\n" +
                "                \"Principio del tercero excluido\",\n" +
                "                \"Principio de razón suficiente\"\n" +
                "            ],\n" +
                "            \"answer\": \"Principio de no contradicción\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"question\": \"¿Cuál es el nombre de la falacia que consiste en atacar a la persona que hace una afirmación en lugar de refutar la afirmación en sí misma?\",\n" +
                "            \"options\": [\n" +
                "                \"Argumento ad ignorantiam\",\n" +
                "                \"Argumento ad populum\",\n" +
                "                \"Argumento ad hominem\",\n" +
                "                \"Argumento ad baculum\"\n" +
                "            ],\n" +
                "            \"answer\": \"Argumento ad hominem\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        ;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ExamChoiceDto examChoiceDto = objectMapper.readValue(json, ExamChoiceDto.class);
            return examChoiceDto;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
