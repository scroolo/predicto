package com.predicto.f1;

import com.predicto.auth.security.JwtUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserF1PredictionController {

    private final F1PredictionRepository predictionRepository;
    private final F1DriverRepository f1DriverRepository;

    @GetMapping("/api/users/me/predictions")
    public ResponseEntity<List<Map<String, Object>>> getMyPredictions(@AuthenticationPrincipal JwtUser jwtUser) {
        if (jwtUser == null) return ResponseEntity.status(401).build();
        var predictions = predictionRepository.findByUserId(jwtUser.id());
        return ResponseEntity.ok(predictions.stream().map(p -> {
            var session = p.getSession();
            var meeting = session.getMeeting();
            var sessionDrivers = f1DriverRepository.findBySessionKey(session.getSessionKey());
            var driverMap = sessionDrivers.stream()
                .collect(Collectors.toMap(F1Driver::getDriverNumber, d -> d.getNameAcronym() != null ? d.getNameAcronym() : "#" + d.getDriverNumber()));
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", p.getId());
            map.put("sessionId", session.getId());
            map.put("sessionName", session.getSessionName());
            map.put("sessionType", session.getSessionType());
            map.put("dateStart", session.getDateStart());
            map.put("meetingName", meeting.getMeetingName());
            map.put("meetingId", meeting.getId());
            map.put("predictedPoleDriverNumber", p.getPredictedPoleDriverNumber());
            map.put("predictedP1DriverNumber", p.getPredictedP1DriverNumber());
            map.put("predictedP2DriverNumber", p.getPredictedP2DriverNumber());
            map.put("predictedP3DriverNumber", p.getPredictedP3DriverNumber());
            map.put("poleDriverName", driverMap.getOrDefault(p.getPredictedPoleDriverNumber(), p.getPredictedPoleDriverNumber() != null ? "#" + p.getPredictedPoleDriverNumber() : "—"));
            map.put("p1DriverName", driverMap.getOrDefault(p.getPredictedP1DriverNumber(), p.getPredictedP1DriverNumber() != null ? "#" + p.getPredictedP1DriverNumber() : "—"));
            map.put("p2DriverName", driverMap.getOrDefault(p.getPredictedP2DriverNumber(), p.getPredictedP2DriverNumber() != null ? "#" + p.getPredictedP2DriverNumber() : "—"));
            map.put("p3DriverName", driverMap.getOrDefault(p.getPredictedP3DriverNumber(), p.getPredictedP3DriverNumber() != null ? "#" + p.getPredictedP3DriverNumber() : "—"));
            map.put("pointsEarned", p.getPointsEarned());
            map.put("status", p.getStatus().name());
            map.put("settledAt", p.getSettledAt());
            map.put("locked", session.getLocked());
            map.put("predictionsLocked", session.getPredictionsLocked());
            return map;
        }).toList());
    }
}
