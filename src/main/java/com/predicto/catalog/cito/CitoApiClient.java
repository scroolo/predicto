package com.predicto.catalog.cito;

import java.util.List;

public interface CitoApiClient {

    List<LeagueDto> fetchLeagues();

    List<TeamDto> fetchTeamsByLeague(String leagueId);

    RosterDto fetchRosterByTeam(String teamSlug);

    List<ScheduledMatchDto> fetchSchedule();

    ScheduledMatchDto fetchMatchDetail(String matchId);
}
