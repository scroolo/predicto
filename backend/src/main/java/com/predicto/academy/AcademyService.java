package com.predicto.academy;

import com.predicto.achievement.AchievementService;
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
    private final AchievementService achievementService;

    @Transactional(readOnly = true)
    public List<Course> getPublishedCourses() {
        return courseRepository.findByPublishedTrueOrderByCategoryAscLevelAsc();
    }

    @Transactional(readOnly = true)
    public List<Course> getCoursesByCategory(AcademyCategory category) {
        return courseRepository.findByCategoryAndPublishedTrueOrderByLevelAsc(category);
    }

    @Transactional(readOnly = true)
    public List<Lesson> getLessonsForCourse(UUID courseId) {
        return lessonRepository.findByCourseIdAndPublishedTrueOrderBySortOrderAsc(courseId);
    }

    @Transactional(readOnly = true)
    public List<QuizQuestion> getQuizForLesson(UUID lessonId) {
        return quizQuestionRepository.findByLessonIdOrderBySortOrderAsc(lessonId);
    }

    @Transactional(readOnly = true)
    public AcademyProgressResponse getUserProgress(UUID userId) {
        List<UserLessonProgress> completed = progressRepository.findByUserIdAndCompletedTrue(userId);
        List<UserCertificate> certificates = certificateRepository.findByUserId(userId);
        Long totalXp = progressRepository.sumXpEarnedByUser(userId);
        var certDtos = certificates.stream()
            .map(c -> new AcademyProgressResponse.CertificateDto(
                c.getCourse().getId().toString(),
                c.getCourse().getTitle()
            ))
            .toList();
        return new AcademyProgressResponse(completed.size(), totalXp != null ? totalXp.intValue() : 0, certDtos);
    }

    @Transactional
    public CompleteLessonResponse completeLesson(UUID userId, UUID lessonId, int quizScore) {
        User user = userRepository.findById(userId).orElseThrow();
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();

        // Check if already completed
        var existing = progressRepository.findByUserIdAndLessonId(userId, lessonId);
        if (existing.isPresent() && existing.get().getCompleted()) {
            // Already completed — just check achievements in case they weren't awarded
            long totalCompleted = progressRepository.findByUserIdAndCompletedTrue(userId).size();
            if (totalCompleted == 1) achievementService.awardById(userId, "academy_first_lesson");
            if (totalCompleted >= 10) achievementService.awardById(userId, "academy_10_lessons");
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

        // Check achievements
        long totalCompleted = progressRepository.findByUserIdAndCompletedTrue(userId).size() + 1;

        if (totalCompleted == 1) {
            achievementService.awardById(userId, "academy_first_lesson");
        }
        if (totalCompleted >= 10) {
            achievementService.awardById(userId, "academy_10_lessons");
        }
        if (certificateAwarded) {
            achievementService.awardById(userId, "academy_first_course");

            String category = lesson.getCourse().getCategory().name();
            List<Course> categoryCourses = courseRepository.findByCategoryAndPublishedTrueOrderByLevelAsc(lesson.getCourse().getCategory());
            long completedCategoryCourses = categoryCourses.stream()
                .filter(c -> certificateRepository.existsByUserIdAndCourseId(userId, c.getId()))
                .count();
            if (completedCategoryCourses >= categoryCourses.size()) {
                String achievementId = switch (category) {
                    case "LOL" -> "academy_lol_complete";
                    case "CS2" -> "academy_cs2_complete";
                    case "F1" -> "academy_f1_complete";
                    default -> null;
                };
                if (achievementId != null) {
                    achievementService.awardById(userId, achievementId);
                }

                boolean allComplete = List.of(AcademyCategory.LOL, AcademyCategory.CS2, AcademyCategory.F1).stream()
                    .allMatch(cat -> {
                        List<Course> courses = courseRepository.findByCategoryAndPublishedTrueOrderByLevelAsc(cat);
                        return courses.stream().allMatch(c -> certificateRepository.existsByUserIdAndCourseId(userId, c.getId()));
                    });
                if (allComplete) {
                    achievementService.awardById(userId, "academy_all_complete");
                }
            }
        }

        return new CompleteLessonResponse(xp, quizScore, certificateAwarded);
    }
}
