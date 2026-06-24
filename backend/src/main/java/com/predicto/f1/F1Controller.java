package com.predicto.f1;

import com.predicto.auth.UserRepository;
import com.predicto.auth.security.JwtUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/f1")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class F1Controller {

    private final F1MeetingRepository meetingRepository;
    private final F1SessionRepository sessionRepository;
    private final F1DriverRepository driverRepository;
    private final F1PredictionRepository predictionRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @GetMapping("/meetings")
    public ResponseEntity<List<Map<String, Object>>> getMeetings(@RequestParam(defaultValue = "2026") int year) {
        var meetings = meetingRepository.findByYearOrderByDateStartAsc(year);
        return ResponseEntity.ok(meetings.stream().map(m -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId());
            map.put("meetingKey", m.getMeetingKey());
            map.put("meetingName", m.getMeetingName());
            map.put("countryName", m.getCountryName());
            map.put("countryFlagUrl", m.getCountryFlagUrl());
            map.put("circuitShortName", m.getCircuitShortName());
            map.put("circuitImageUrl", m.getCircuitImageUrl());
            map.put("dateStart", m.getDateStart());
            map.put("dateEnd", m.getDateEnd());
            map.put("isCancelled", m.getIsCancelled());
            return map;
        }).toList());
    }

    @GetMapping("/meetings/{id}/sessions")
    public ResponseEntity<List<Map<String, Object>>> getMeetingSessions(@PathVariable UUID id) {
        var sessions = sessionRepository.findByMeetingIdOrderByDateStartAsc(id);
        return ResponseEntity.ok(sessions.stream().map(s -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", s.getId());
            map.put("sessionKey", s.getSessionKey());
            map.put("sessionName", s.getSessionName());
            map.put("sessionType", s.getSessionType());
            map.put("dateStart", s.getDateStart());
            map.put("dateEnd", s.getDateEnd());
            map.put("status", s.getStatus().name());
            return map;
        }).toList());
    }

    @GetMapping("/sessions/{id}/drivers")
    public ResponseEntity<List<Map<String, Object>>> getSessionDrivers(@PathVariable UUID id) {
        var session = sessionRepository.findById(id).orElse(null);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        var drivers = driverRepository.findBySessionKey(session.getSessionKey());
        return ResponseEntity.ok(drivers.stream().map(d -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("driverNumber", d.getDriverNumber());
            map.put("fullName", d.getFullName());
            map.put("nameAcronym", d.getNameAcronym());
            map.put("headshotUrl", d.getHeadshotUrl());
            map.put("teamName", d.getTeamName());
            map.put("teamColour", d.getTeamColour());
            return map;
        }).toList());
    }

    @GetMapping("/sessions/upcoming")
    public ResponseEntity<List<Map<String, Object>>> getUpcomingSessions(
            @RequestParam(defaultValue = "5") int limit) {
        var sessions = sessionRepository.findUpcomingSessions(
                OffsetDateTime.now(), PageRequest.of(0, limit));
        return ResponseEntity.ok(sessions.stream().map(s -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", s.getId());
            map.put("sessionKey", s.getSessionKey());
            map.put("sessionName", s.getSessionName());
            map.put("sessionType", s.getSessionType());
            map.put("dateStart", s.getDateStart());
            map.put("status", s.getStatus().name());
            map.put("meetingName", s.getMeeting().getMeetingName());
            return map;
        }).toList());
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<Map<String, Object>> getSession(@PathVariable UUID id) {
        var session = sessionRepository.findById(id).orElse(null);
        if (session == null) return ResponseEntity.notFound().build();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", session.getId());
        map.put("sessionKey", session.getSessionKey());
        map.put("sessionName", session.getSessionName());
        map.put("sessionType", session.getSessionType());
        map.put("dateStart", session.getDateStart());
        map.put("dateEnd", session.getDateEnd());
        map.put("status", session.getStatus().name());
        map.put("locked", session.getLocked());
        map.put("predictionsLocked", session.getPredictionsLocked());
        map.put("meetingName", session.getMeeting().getMeetingName());
        map.put("meetingId", session.getMeeting().getId());
        map.put("resultP1DriverNumber", session.getResultP1DriverNumber());
        map.put("resultP2DriverNumber", session.getResultP2DriverNumber());
        map.put("resultP3DriverNumber", session.getResultP3DriverNumber());
        map.put("resultPoleDriverNumber", session.getResultPoleDriverNumber());
        return ResponseEntity.ok(map);
    }

    @Transactional
    @PostMapping("/sessions/{id}/predictions")
    public ResponseEntity<?> submitPrediction(@AuthenticationPrincipal JwtUser jwtUser,
                                               @PathVariable UUID id,
                                               @RequestBody Map<String, Object> body) {
        System.out.println("F1 PREDICT: jwtUser=" + jwtUser + " sessionId=" + id);
        if (jwtUser == null) return ResponseEntity.status(401).build();

        var session = sessionRepository.findById(id).orElse(null);
        if (session == null) return ResponseEntity.notFound().build();
        if (session.getStatus() != F1SessionStatus.UPCOMING) {
            return ResponseEntity.badRequest().body(Map.of("message", "Session is not upcoming"));
        }
        if (Boolean.TRUE.equals(session.getPredictionsLocked())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Predictions are locked"));
        }

        var user = userRepository.findById(jwtUser.id()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();

        var prediction = predictionRepository.findByUserIdAndSessionId(user.getId(), session.getId())
                .orElse(F1Prediction.builder()
                        .user(user)
                        .session(session)
                        .status(F1PredictionStatus.PENDING)
                        .pointsEarned(0)
                        .build());

        if (body.containsKey("predictedPoleDriverNumber"))
            prediction.setPredictedPoleDriverNumber(toInt(body.get("predictedPoleDriverNumber")));
        if (body.containsKey("predictedP1DriverNumber"))
            prediction.setPredictedP1DriverNumber(toInt(body.get("predictedP1DriverNumber")));
        if (body.containsKey("predictedP2DriverNumber"))
            prediction.setPredictedP2DriverNumber(toInt(body.get("predictedP2DriverNumber")));
        if (body.containsKey("predictedP3DriverNumber"))
            prediction.setPredictedP3DriverNumber(toInt(body.get("predictedP3DriverNumber")));
        if (prediction.getCreatedAt() == null) prediction.setCreatedAt(OffsetDateTime.now());
        prediction.setUpdatedAt(OffsetDateTime.now());
        prediction = predictionRepository.save(prediction);

        return ResponseEntity.ok(predictionToMap(prediction));
    }

    @GetMapping("/sessions/{id}/predictions/me")
    public ResponseEntity<?> getMyPrediction(@AuthenticationPrincipal JwtUser jwtUser,
                                              @PathVariable UUID id) {
        if (jwtUser == null) return ResponseEntity.status(401).build();
        var prediction = predictionRepository.findByUserIdAndSessionId(jwtUser.id(), id).orElse(null);
        if (prediction == null) return ResponseEntity.ok(Map.of());
        return ResponseEntity.ok(predictionToMap(prediction));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard() {
        Query query = entityManager.createNativeQuery(
                "SELECT user_id, username, avatar_url, total_points, total_predictions, correct_predictions " +
                "FROM f1_leaderboard");
        List<Object[]> rows = query.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("userId", row[0]);
            map.put("username", row[1]);
            map.put("avatarUrl", row[2]);
            map.put("totalPoints", row[3]);
            map.put("totalPredictions", row[4]);
            map.put("correctPredictions", row[5]);
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> predictionToMap(F1Prediction p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("sessionId", p.getSession().getId());
        map.put("predictedPoleDriverNumber", p.getPredictedPoleDriverNumber());
        map.put("predictedP1DriverNumber", p.getPredictedP1DriverNumber());
        map.put("predictedP2DriverNumber", p.getPredictedP2DriverNumber());
        map.put("predictedP3DriverNumber", p.getPredictedP3DriverNumber());
        map.put("pointsEarned", p.getPointsEarned());
        map.put("status", p.getStatus().name());
        map.put("settledAt", p.getSettledAt());
        map.put("createdAt", p.getCreatedAt());
        map.put("updatedAt", p.getUpdatedAt());
        return map;
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(value.toString()); } catch (Exception e) { return null; }
    }
}
