package com.predicto.catalog.pandascore;

import java.util.List;

public interface PandaScoreApiClient {

    List<PandaScoreMatch> fetchLolUpcoming();

    List<PandaScoreMatch> fetchLolRunning();

    List<PandaScoreMatch> fetchLolPast();

    List<PandaScoreLeague> fetchLolLeagues();

    List<PandaScoreTeam> fetchLolTeams();

    List<PandaScoreMatch> fetchCs2Upcoming();

    List<PandaScoreMatch> fetchCs2Running();

    List<PandaScoreMatch> fetchCs2Past();

    List<PandaScoreMatch> fetchPastMatches(String game, int page, int perPage);

    List<PandaScoreLeague> fetchCs2Leagues();

    List<PandaScoreTeam> fetchCs2Teams();
}
