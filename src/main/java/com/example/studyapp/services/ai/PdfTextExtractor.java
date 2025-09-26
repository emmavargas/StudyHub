package com.example.studyapp.services.ai;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
@Component
public class PdfTextExtractor {

    private static final int MAX_TEXT_LENGTH = 10000000; // Límite de caracteres para la IA
    private static final int MIN_TEXT_LENGTH = 50; // Umbral para considerar texto válido

    public String extractText(String filePath) {
        File pdfFile = new File(filePath);

        // Validar existencia y permisos del archivo
        if (!pdfFile.exists() || !pdfFile.canRead()) {
            return "[Archivo no accesible: " + filePath + "]";
        }

        // Extraer texto con PDFBox
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String pdfText = stripper.getText(document).trim();
            // Eliminar todos los saltos de línea (\n) y reemplazar por un espacio
            pdfText = pdfText.replaceAll("\\n", " ");
            // Opcionalmente, puedes eliminar espacios múltiples si lo deseas
            pdfText = pdfText.replaceAll("\\s+", " ");
            if (pdfText.length() < MIN_TEXT_LENGTH) {
                return "[No se encontró texto nativo suficiente en el PDF, podría ser un documento escaneado: " + filePath + "]";
            }
            if (pdfText.length() > MAX_TEXT_LENGTH) {
                pdfText = pdfText.substring(0, MAX_TEXT_LENGTH) + "... [Texto truncado]";
            }
            return pdfText;
        } catch (IOException e) {
            return "[Error al extraer texto del PDF: " + e.getMessage() + "]";
        }
    }
}