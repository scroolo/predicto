package com.predicto.academy;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/academy")
@RequiredArgsConstructor
public class AdminAcademyController {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final GeminiLessonService geminiLessonService;

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> listCourses() {
        return ResponseEntity.ok(courseRepository.findAll());
    }

    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseRepository.save(course));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable UUID id, @RequestBody Course req) {
        var course = courseRepository.findById(id).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();
        course.setTitle(req.getTitle());
        course.setDescription(req.getDescription());
        course.setCategory(req.getCategory());
        course.setLevel(req.getLevel());
        course.setXpReward(req.getXpReward());
        course.setPublished(req.getPublished());
        course.setImageUrl(req.getImageUrl());
        return ResponseEntity.ok(courseRepository.save(course));
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID id) {
        courseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<List<Lesson>> listLessons(@PathVariable UUID courseId) {
        return ResponseEntity.ok(lessonRepository.findByCourseIdOrderBySortOrderAsc(courseId));
    }

    @PostMapping("/courses/{courseId}/lessons/generate")
    public ResponseEntity<?> generateLesson(
            @PathVariable UUID courseId,
            @RequestBody GenerateLessonRequest req) {
        var course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();

        var result = geminiLessonService.generateLesson(req.topic(), course.getCategory(), course.getLevel());

        Lesson lesson = Lesson.builder()
                .course(course)
                .title(result.title())
                .summary(result.summary())
                .content(result.content())
                .sortOrder(req.sortOrder() != null ? req.sortOrder() : 0)
                .published(false)
                .build();
        lesson = lessonRepository.save(lesson);

        for (var q : result.questions()) {
            List<String> opts = new ArrayList<>(List.of(q.optionA(), q.optionB(), q.optionC(), q.optionD()));
            Collections.shuffle(opts);
            int correctIdx = opts.indexOf(q.optionA());
            String correctLetter = List.of("A","B","C","D").get(correctIdx);

            QuizQuestion question = QuizQuestion.builder()
                    .lesson(lesson)
                    .question(q.question())
                    .optionA(opts.get(0))
                    .optionB(opts.get(1))
                    .optionC(opts.get(2))
                    .optionD(opts.get(3))
                    .correctOption(correctLetter)
                    .sortOrder(q.sortOrder())
                    .build();
            quizQuestionRepository.save(question);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(lesson);
    }

    @PatchMapping("/lessons/{id}/publish")
    public ResponseEntity<Lesson> publishLesson(@PathVariable UUID id) {
        var lesson = lessonRepository.findById(id).orElse(null);
        if (lesson == null) return ResponseEntity.notFound().build();
        lesson.setPublished(true);
        return ResponseEntity.ok(lessonRepository.save(lesson));
    }

    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable UUID id) {
        lessonRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
