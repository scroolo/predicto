package com.predicto.admin;

import com.predicto.f1.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/admin/f1")
@RequiredArgsConstructor
public class AdminF1Controller {

    private final F1SyncService f1SyncService;
    private final F1SessionRepository sessionRepository;
    private final F1PredictionRepository predictionRepository;
    private final F1DriverRepository driverRepository;

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> sync(@RequestParam(defaultValue = "2026") int year) {
        int items = f1SyncService.syncMeetingsAndSessions(year);
        return ResponseEntity.ok(Map.of(
                "job", "f1-sync-" + year,
                "status", "completed",
                "items", items
        ));
    }

    @PostMapping("/sessions/{id}/lock")
    public ResponseEntity<?> lockSession(@PathVariable UUID id) {
        var session = sessionRepository.findById(id).orElse(null);
        if (session == null) return ResponseEntity.notFound().build();
        session.setLocked(true);
        session.setPredictionsLocked(true);
        sessionRepository.save(session);
        return ResponseEntity.ok(Map.of("message", "Session locked"));
    }

    @PostMapping("/sessions/{id}/settle")
    public ResponseEntity<?> settleSession(@PathVariable UUID id,
                                           @RequestBody Map<String, Object> body) {
        var session = sessionRepository.findById(id).orElse(null);
        if (session == null) return ResponseEntity.notFound().build();

        Integer actualPole = toInt(body.get("resultPoleDriverNumber"));
        Integer actualP1 = toInt(body.get("resultP1DriverNumber"));
        Integer actualP2 = toInt(body.get("resultP2DriverNumber"));
        Integer actualP3 = toInt(body.get("resultP3DriverNumber"));

        session.setResultPoleDriverNumber(actualPole);
        session.setResultP1DriverNumber(actualP1);
        session.setResultP2DriverNumber(actualP2);
        session.setResultP3DriverNumber(actualP3);

        var predictions = predictionRepository.findBySessionId(session.getId());
        for (var p : predictions) {
            int points = 0;
            if (actualPole != null && actualPole.equals(p.getPredictedPoleDriverNumber())) points += 15;
            if (actualP1 != null && actualP1.equals(p.getPredictedP1DriverNumber())) points += 20;
            if (actualP2 != null && actualP2.equals(p.getPredictedP2DriverNumber())) points += 10;
            if (actualP3 != null && actualP3.equals(p.getPredictedP3DriverNumber())) points += 10;

            boolean podiumCorrect = actualP1 != null && actualP1.equals(p.getPredictedP1DriverNumber())
                    && actualP2 != null && actualP2.equals(p.getPredictedP2DriverNumber())
                    && actualP3 != null && actualP3.equals(p.getPredictedP3DriverNumber());
            if (podiumCorrect) points += 20;

            p.setPointsEarned(points);
            p.setStatus(F1PredictionStatus.SETTLED);
            p.setSettledAt(LocalDateTime.now());
            p.setUpdatedAt(OffsetDateTime.now());
            predictionRepository.save(p);
        }

        session.setStatus(F1SessionStatus.FINISHED);
        sessionRepository.save(session);

        return ResponseEntity.ok(Map.of("message", "Session settled", "predictionsSettled", predictions.size()));
    }

    @GetMapping("/predictions")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getAllF1Predictions() {
        var predictions = predictionRepository.findAll();
        var drivers = driverRepository.findAll();
        var driverMap = new HashMap<Integer, String>();
        for (var d : drivers) {
            driverMap.put(d.getDriverNumber(), d.getFullName());
        }
        var result = new ArrayList<Map<String, Object>>();
        for (var p : predictions) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", p.getId());
            map.put("userId", p.getUser().getId());
            map.put("username", p.getUser().getUsername());
            var session = p.getSession();
            map.put("sessionId", session.getId());
            map.put("sessionName", session.getSessionName());
            map.put("meetingName", session.getMeeting().getMeetingName());
            map.put("dateStart", session.getDateStart());
            map.put("predictedPoleDriverNumber", p.getPredictedPoleDriverNumber());
            map.put("predictedPoleDriverName", driverMap.get(p.getPredictedPoleDriverNumber()));
            map.put("predictedP1DriverNumber", p.getPredictedP1DriverNumber());
            map.put("predictedP1DriverName", driverMap.get(p.getPredictedP1DriverNumber()));
            map.put("predictedP2DriverNumber", p.getPredictedP2DriverNumber());
            map.put("predictedP2DriverName", driverMap.get(p.getPredictedP2DriverNumber()));
            map.put("predictedP3DriverNumber", p.getPredictedP3DriverNumber());
            map.put("predictedP3DriverName", driverMap.get(p.getPredictedP3DriverNumber()));
            map.put("pointsEarned", p.getPointsEarned());
            map.put("status", p.getStatus().name());
            map.put("settledAt", p.getSettledAt());
            map.put("createdAt", p.getCreatedAt());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(value.toString()); } catch (Exception e) { return null; }
    }
}
