package com.predicto.f1.openf1;

import java.util.List;

public interface OpenF1ApiClient {
    List<OpenF1Meeting> fetchMeetings(int year);
    List<OpenF1Session> fetchSessions(int meetingKey);
    List<OpenF1Session> fetchSessionsByYear(int year);
    List<OpenF1Driver> fetchDrivers(int sessionKey);
    List<OpenF1SessionResult> fetchSessionResults(int sessionKey);
}
