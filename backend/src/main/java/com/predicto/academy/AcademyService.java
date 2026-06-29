package com.predicto.academy;

import com.predicto.auth.User;
import com.predicto.auth.UserRepository;
import com.predicto.auth.security.JwtUser;
import com.predicto.wallet.Wallet;
import com.predicto.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AcademyService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final UserLessonProgressRepository progressRepository;
    private final UserCertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public List<Course> getPublishedCourses() {
        return courseRepository.findByPublishedTrueOrderByCategoryAscLevelAsc();
    }

    public List<Course> getCoursesByCategory(AcademyCategory category) {
        return courseRepository.findByCategoryAndPublishedTrueOrderByLevelAsc(category);
    }

    public List<Lesson> getLessonsForCourse(UUID courseId) {
        return lessonRepository.findByCourseIdAndPublishedTrueOrderBySortOrderAsc(courseId);
    }

    public List<QuizQuestion> getQuizForLesson(UUID lessonId) {
        return quizQuestionRepository.findByLessonIdOrderBySortOrderAsc(lessonId);
    }

    public AcademyProgressResponse getUserProgress(UUID userId) {
        List<UserLessonProgress> completed = progressRepository.findByUserIdAndCompletedTrue(userId);
        List<UserCertificate> certificates = certificateRepository.findByUserId(userId);
        Integer totalXp = progressRepository.sumXpEarnedByUser(userId);
        return new AcademyProgressResponse(completed.size(), totalXp != null ? totalXp : 0, certificates);
    }

    @Transactional
    public CompleteLessonResponse completeLesson(UUID userId, UUID lessonId, int quizScore) {
        User user = userRepository.findById(userId).orElseThrow();
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();

        // Check if already completed
        var existing = progressRepository.findByUserIdAndLessonId(userId, lessonId);
        if (existing.isPresent() && existing.get().getCompleted()) {
            return new CompleteLessonResponse(0, existing.get().getQuizScore(), false);
        }

        // Calculate XP
        int xp = lesson.getXpReward();
        if (quizScore >= 5) xp += lesson.getXpQuizBonus();
        else if (quizScore >= 3) xp += lesson.getXpQuizBonus() / 3;

        // Save progress
        UserLessonProgress progress = existing.orElse(UserLessonProgress.builder()
                .user(user)
                .lesson(lesson)
                .build());
        progress.setCompleted(true);
        progress.setQuizScore(quizScore);
        progress.setCompletedAt(OffsetDateTime.now());
        progress.setXpEarned(xp);
        progressRepository.save(progress);

        // Award XP to wallet
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();
        wallet.setAcademyXp(wallet.getAcademyXp() + xp);
        walletRepository.save(wallet);

        // Check if course completed — award certificate
        boolean certificateAwarded = false;
        Course course = lesson.getCourse();
        long totalLessons = lessonRepository.countByCourseIdAndPublishedTrue(course.getId());
        long completedLessons = progressRepository.countCompletedLessonsByCourse(userId, course.getId());

        if (completedLessons >= totalLessons && !certificateRepository.existsByUserIdAndCourseId(userId, course.getId())) {
            UserCertificate cert = UserCertificate.builder()
                    .user(user)
                    .course(course)
                    .issuedAt(OffsetDateTime.now())
                    .build();
            certificateRepository.save(cert);
            wallet.setAcademyXp(wallet.getAcademyXp() + course.getXpReward());
            walletRepository.save(wallet);
            certificateAwarded = true;
            log.info("Certificate awarded: userId={} courseId={}", userId, course.getId());
        }

        log.info("Lesson completed: userId={} lessonId={} xp={} quizScore={}", userId, lessonId, xp, quizScore);
        return new CompleteLessonResponse(xp, quizScore, certificateAwarded);
    }
}
