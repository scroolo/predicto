package com.predicto.academy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GeminiLessonService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiLessonResult generateLesson(String topic, AcademyCategory category, AcademyLevel level) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY not configured");
        }

        String categoryName = switch (category) {
            case LOL -> "League of Legends";
            case CS2 -> "Counter-Strike 2";
            case F1 -> "Formula 1";
        };

        String levelName = switch (level) {
            case BEGINNER -> "Začiatočník (úplné základy, jednoduchý jazyk)";
            case ADVANCED -> "Pokročilý (stratégie, taktiky, štatistiky)";
            case EXPERT -> "Expert (detailné analýzy, pokročilé koncepty)";
        };

        String prompt = """
            Si lektor pre Predicto Academy — vzdelávaciu platformu pre fanúšikov esportu a motorsportu.
            Vytvor lekciu v SLOVENSKOM jazyku na tému: "%s"
            Šport: %s
            Úroveň: %s

            Lekcia musí byť zaujímavá, praktická a zrozumiteľná.
            Dĺžka obsahu: 300-500 slov.

            Odpovedz PRESNE v tomto formáte bez akýchkoľvek iných slov:
            TITLE: [názov lekcie]
            SUMMARY: [1-2 vety zhrnutie]
            CONTENT: [obsah lekcie]
            Q1: [otázka 1]
            Q1A: [správna odpoveď]
            Q1B: [nesprávna odpoveď]
            Q1C: [nesprávna odpoveď]
            Q1D: [nesprávna odpoveď]
            Q2: [otázka 2]
            Q2A: [správna odpoveď]
            Q2B: [nesprávna odpoveď]
            Q2C: [nesprávna odpoveď]
            Q2D: [nesprávna odpoveď]
            Q3: [otázka 3]
            Q3A: [správna odpoveď]
            Q3B: [nesprávna odpoveď]
            Q3C: [nesprávna odpoveď]
            Q3D: [nesprávna odpoveď]
            Q4: [otázka 4]
            Q4A: [správna odpoveď]
            Q4B: [nesprávna odpoveď]
            Q4C: [nesprávna odpoveď]
            Q4D: [nesprávna odpoveď]
            Q5: [otázka 5]
            Q5A: [správna odpoveď]
            Q5B: [nesprávna odpoveď]
            Q5C: [nesprávna odpoveď]
            Q5D: [nesprávna odpoveď]
            """.formatted(topic, categoryName, levelName);

        try {
            String requestBody = """
                {
                    "contents": [{
                        "parts": [{"text": %s}]
                    }]
                }
                """.formatted(objectMapper.writeValueAsString(prompt));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Gemini API error: " + response.statusCode() + " " + response.body());
            }

            var root = objectMapper.readTree(response.body());
            String text = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

            return parseGeminiResponse(text);

        } catch (Exception e) {
            log.error("Gemini lesson generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate lesson: " + e.getMessage());
        }
    }

    private GeminiLessonResult parseGeminiResponse(String text) {
        String title = extractLine(text, "TITLE:");
        String summary = extractLine(text, "SUMMARY:");
        String content = extractContent(text);
        List<QuizQuestionData> questions = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String q = extractLine(text, "Q" + i + ":");
            String a = extractLine(text, "Q" + i + "A:");
            String b = extractLine(text, "Q" + i + "B:");
            String c = extractLine(text, "Q" + i + "C:");
            String d = extractLine(text, "Q" + i + "D:");
            if (q != null && !q.isBlank()) {
                questions.add(new QuizQuestionData(q, a, b, c, d, "A", i));
            }
        }

        return new GeminiLessonResult(title, summary, content, questions);
    }

    private String extractLine(String text, String prefix) {
        for (String line : text.split("\n")) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }
        return "";
    }

    private String extractContent(String text) {
        StringBuilder content = new StringBuilder();
        boolean inContent = false;
        for (String line : text.split("\n")) {
            if (line.startsWith("CONTENT:")) {
                content.append(line.substring("CONTENT:".length()).trim());
                inContent = true;
            } else if (inContent && line.startsWith("Q1:")) {
                break;
            } else if (inContent) {
                content.append("\n").append(line);
            }
        }
        return content.toString().trim();
    }
}
