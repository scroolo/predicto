package com.predicto.f1;

import com.predicto.catalog.sync.SyncRun;
import com.predicto.catalog.sync.SyncRunRepository;
import com.predicto.catalog.sync.SyncRunStatus;
import com.predicto.f1.openf1.OpenF1ApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class F1SyncService {

    private final OpenF1ApiClient openF1ApiClient;
    private final F1MeetingRepository meetingRepository;
    private final F1SessionRepository sessionRepository;
    private final F1DriverRepository driverRepository;
    private final SyncRunRepository syncRunRepository;

    @Scheduled(cron = "0 0 6 * * *")
    public void scheduledF1Sync() {
        int year = Year.now().getValue();
        log.info("Starting scheduled F1 sync for year {}", year);
        syncMeetingsAndSessions(year);
    }

    @Transactional
    public int syncMeetingsAndSessions(int year) {
        SyncRun run = SyncRun.builder()
                .jobName("f1-sync-" + year)
                .startedAt(OffsetDateTime.now())
                .status(SyncRunStatus.PARTIAL)
                .itemsProcessed(0)
                .build();
        run = syncRunRepository.save(run);

        int total = 0;
        try {
            var meetings = openF1ApiClient.fetchMeetings(year);
            log.info("Fetched {} meetings for {}", meetings.size(), year);

            for (var om : meetings) {
                var meeting = meetingRepository.findByMeetingKey(om.getMeetingKey())
                        .orElse(new F1Meeting());
                meeting.setMeetingKey(om.getMeetingKey());
                meeting.setMeetingName(om.getMeetingName());
                meeting.setMeetingOfficialName(om.getMeetingOfficialName());
                meeting.setCountryName(om.getCountryName());
                meeting.setCountryFlagUrl(om.getCountryFlag());
                meeting.setCircuitShortName(om.getCircuitShortName());
                meeting.setCircuitImageUrl(om.getCircuitImage());
                meeting.setLocation(om.getLocation());
                meeting.setDateStart(OffsetDateTime.parse(om.getDateStart(), DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                if (om.getDateEnd() != null) {
                    meeting.setDateEnd(OffsetDateTime.parse(om.getDateEnd(), DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                }
                meeting.setYear(om.getYear() != null ? om.getYear() : year);
                meeting.setIsCancelled(om.getIsCancelled() != null && om.getIsCancelled());
                if (meeting.getCreatedAt() == null) {
                    meeting.setCreatedAt(OffsetDateTime.now());
                }
                meeting = meetingRepository.save(meeting);
                total++;
            }

            var allSessions = openF1ApiClient.fetchSessionsByYear(year);
            log.info("Fetched {} sessions for {}", allSessions.size(), year);

            for (var os : allSessions) {
                var meeting = meetingRepository.findByMeetingKey(os.getMeetingKey()).orElse(null);
                if (meeting == null) {
                    log.warn("No meeting found for meeting_key {}, skipping session {}", os.getMeetingKey(), os.getSessionKey());
                    continue;
                }

                var session = sessionRepository.findBySessionKey(os.getSessionKey())
                        .orElse(new F1Session());
                session.setSessionKey(os.getSessionKey());
                session.setMeeting(meeting);
                session.setSessionName(os.getSessionName());
                session.setSessionType(os.getSessionType());
                session.setDateStart(OffsetDateTime.parse(os.getDateStart(), DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                if (os.getDateEnd() != null) {
                    session.setDateEnd(OffsetDateTime.parse(os.getDateEnd(), DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                }
                session.setIsCancelled(os.getIsCancelled() != null && os.getIsCancelled());

                if (session.getStatus() == null) {
                    session.setStatus(F1SessionStatus.UPCOMING);
                }

                if (session.getCreatedAt() == null) {
                    session.setCreatedAt(OffsetDateTime.now());
                }
                if (session.getStatus() == F1SessionStatus.UPCOMING
                        && session.getDateEnd() != null
                        && session.getDateEnd().isBefore(OffsetDateTime.now())) {
                    session.setStatus(F1SessionStatus.FINISHED);
                }

                if (session.getStatus() == F1SessionStatus.UPCOMING && session.getDateStart().isBefore(OffsetDateTime.now())) {
                    var results = openF1ApiClient.fetchSessionResults(os.getSessionKey());
                    if (results.stream().anyMatch(r -> r.getPosition() != null && r.getPosition() == 1)) {
                        session.setStatus(F1SessionStatus.FINISHED);
                        for (var r : results) {
                            if (r.getPosition() != null) {
                                if (r.getPosition() == 1) session.setResultP1DriverNumber(r.getDriverNumber());
                                else if (r.getPosition() == 2) session.setResultP2DriverNumber(r.getDriverNumber());
                                else if (r.getPosition() == 3) session.setResultP3DriverNumber(r.getDriverNumber());
                            }
                        }
                        log.info("Session {} results: P1={}, P2={}, P3={}",
                                session.getSessionKey(),
                                session.getResultP1DriverNumber(),
                                session.getResultP2DriverNumber(),
                                session.getResultP3DriverNumber());
                    }
                }

                if (session.getStatus() == F1SessionStatus.FINISHED && session.getResultP1DriverNumber() == null) {
                    var results = openF1ApiClient.fetchSessionResults(os.getSessionKey());
                    for (var r : results) {
                        if (r.getPosition() != null) {
                            if (r.getPosition() == 1) session.setResultP1DriverNumber(r.getDriverNumber());
                            else if (r.getPosition() == 2) session.setResultP2DriverNumber(r.getDriverNumber());
                            else if (r.getPosition() == 3) session.setResultP3DriverNumber(r.getDriverNumber());
                        }
                    }
                    log.info("Session {} results: P1={}, P2={}, P3={}",
                            session.getSessionKey(),
                            session.getResultP1DriverNumber(),
                            session.getResultP2DriverNumber(),
                            session.getResultP3DriverNumber());
                }

                sessionRepository.save(session);
                total++;

                int driverCount = syncDriversForSession(os.getSessionKey());
                total += driverCount;
            }

            run.setStatus(SyncRunStatus.SUCCESS);
            run.setItemsProcessed(total);
            run.setFinishedAt(OffsetDateTime.now());
            syncRunRepository.save(run);
        } catch (Exception e) {
            log.error("F1 sync failed for year {}", year, e);
            run.setStatus(SyncRunStatus.FAILED);
            run.setErrorMessage(e.getMessage());
            run.setFinishedAt(OffsetDateTime.now());
            syncRunRepository.save(run);
        }

        return total;
    }

    @Transactional
    public int syncDriversForSession(int sessionKey) {
        var drivers = openF1ApiClient.fetchDrivers(sessionKey);
        int count = 0;
        for (var od : drivers) {
            var driver = driverRepository.findByDriverNumberAndSessionKey(od.getDriverNumber(), od.getSessionKey())
                    .orElse(new F1Driver());
            driver.setDriverNumber(od.getDriverNumber());
            driver.setSessionKey(od.getSessionKey());
            driver.setFullName(od.getFullName());
            driver.setNameAcronym(od.getNameAcronym());
            driver.setHeadshotUrl(od.getHeadshotUrl());
            driver.setTeamName(od.getTeamName());
            driver.setTeamColour(od.getTeamColour());
            driverRepository.save(driver);
            count++;
        }
        return count;
    }
}
