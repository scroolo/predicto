package com.predicto.academy;

import com.predicto.auth.security.JwtUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/academy")
@RequiredArgsConstructor
public class AcademyController {

    private final AcademyService academyService;
    private final LessonRepository lessonRepository;
    private final UserLessonProgressRepository progressRepository;

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getCourses() {
        return ResponseEntity.ok(academyService.getPublishedCourses());
    }

    @GetMapping("/courses/category/{category}")
    public ResponseEntity<List<Course>> getCoursesByCategory(@PathVariable AcademyCategory category) {
        return ResponseEntity.ok(academyService.getCoursesByCategory(category));
    }

    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<List<Lesson>> getLessons(@PathVariable UUID courseId) {
        return ResponseEntity.ok(academyService.getLessonsForCourse(courseId));
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<Lesson> getLesson(@PathVariable UUID lessonId) {
        return lessonRepository.findById(lessonId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/lessons/{lessonId}/quiz")
    public ResponseEntity<List<QuizQuestion>> getQuiz(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(academyService.getQuizForLesson(lessonId));
    }

    @GetMapping("/progress")
    public ResponseEntity<AcademyProgressResponse> getProgress(
            @AuthenticationPrincipal JwtUser jwtUser) {
        return ResponseEntity.ok(academyService.getUserProgress(jwtUser.id()));
    }

    @GetMapping("/progress/lessons")
    public ResponseEntity<List<UUID>> getCompletedLessonIds(@AuthenticationPrincipal JwtUser jwtUser) {
        if (jwtUser == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(progressRepository.findCompletedLessonIdsByUserId(jwtUser.id()));
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<CompleteLessonResponse> completeLesson(
            @PathVariable UUID lessonId,
            @RequestBody CompleteLessonRequest request,
            @AuthenticationPrincipal JwtUser jwtUser) {
        return ResponseEntity.ok(academyService.completeLesson(jwtUser.id(), lessonId, request.quizScore()));
    }
}
