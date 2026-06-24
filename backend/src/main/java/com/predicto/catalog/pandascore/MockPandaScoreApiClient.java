package com.predicto.catalog.pandascore;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "pandascore.mock-enabled", havingValue = "true", matchIfMissing = true)
public class MockPandaScoreApiClient implements PandaScoreApiClient {

    private List<PandaScoreLeague> lolLeagues;
    private List<PandaScoreTeam> lolTeams;
    private List<PandaScoreLeague> cs2Leagues;
    private List<PandaScoreTeam> cs2Teams;

    @PostConstruct
    public void init() {
        initLol();
        initCs2();
    }

    private void initLol() {
        lolLeagues = List.of(
            new PandaScoreLeague(101, "LCK", "lck", "https://cdn.pandascore.co/images/league/image/101/lck.png"),
            new PandaScoreLeague(102, "LEC", "lec", "https://cdn.pandascore.co/images/league/image/102/lec.png"),
            new PandaScoreLeague(103, "LCS", "lcs", "https://cdn.pandascore.co/images/league/image/103/lcs.png"),
            new PandaScoreLeague(104, "LPL", "lpl", "https://cdn.pandascore.co/images/league/image/104/lpl.png")
        );

        var t1 = new PandaScoreTeam(1001, "T1", "t1", "https://cdn.pandascore.co/images/team/image/1001/t1.png", List.of(
            new PandaScorePlayer(9001, "Faker", "faker", "mid", "https://cdn.pandascore.co/images/player/image/9001/faker.png", null),
            new PandaScorePlayer(9002, "Oner", "oner", "jungle", "https://cdn.pandascore.co/images/player/image/9002/oner.png", null),
            new PandaScorePlayer(9003, "Gumayusi", "gumayusi", "adc", "https://cdn.pandascore.co/images/player/image/9003/gumayusi.png", null),
            new PandaScorePlayer(9004, "Keria", "keria", "support", "https://cdn.pandascore.co/images/player/image/9004/keria.png", null),
            new PandaScorePlayer(9005, "Doran", "doran", "top", "https://cdn.pandascore.co/images/player/image/9005/doran.png", null)
        ));

        var geng = new PandaScoreTeam(1002, "Gen.G", "geng", "https://cdn.pandascore.co/images/team/image/1002/geng.png", List.of(
            new PandaScorePlayer(9006, "Chovy", "chovy", "mid", "https://cdn.pandascore.co/images/player/image/9006/chovy.png", null),
            new PandaScorePlayer(9007, "Canyon", "canyon", "jungle", "https://cdn.pandascore.co/images/player/image/9007/canyon.png", null)
        ));

        var hle = new PandaScoreTeam(1003, "Hanwha Life Esports", "hanwha-life", "https://cdn.pandascore.co/images/team/image/1003/hle.png", List.of(
            new PandaScorePlayer(9008, "Zeka", "zeka", "mid", "https://cdn.pandascore.co/images/player/image/9008/zeka.png", null),
            new PandaScorePlayer(9009, "Viper", "viper", "adc", "https://cdn.pandascore.co/images/player/image/9009/viper.png", null),
            new PandaScorePlayer(9010, "Peanut", "peanut", "jungle", "https://cdn.pandascore.co/images/player/image/9010/peanut.png", null)
        ));

        var g2 = new PandaScoreTeam(1004, "G2 Esports", "g2", "https://cdn.pandascore.co/images/team/image/1004/g2.png", List.of(
            new PandaScorePlayer(9011, "Caps", "caps", "mid", "https://cdn.pandascore.co/images/player/image/9011/caps.png", null),
            new PandaScorePlayer(9012, "Hans Sama", "hans-sama", "adc", "https://cdn.pandascore.co/images/player/image/9012/hans-sama.png", null)
        ));

        var fnc = new PandaScoreTeam(1005, "Fnatic", "fnatic", "https://cdn.pandascore.co/images/team/image/1005/fnc.png", List.of(
            new PandaScorePlayer(9013, "Humanoid", "humanoid", "mid", "https://cdn.pandascore.co/images/player/image/9013/humanoid.png", null),
            new PandaScorePlayer(9014, "Razork", "razork", "jungle", "https://cdn.pandascore.co/images/player/image/9014/razork.png", null)
        ));

        var c9 = new PandaScoreTeam(1006, "Cloud9", "cloud9", "https://cdn.pandascore.co/images/team/image/1006/c9.png", List.of(
            new PandaScorePlayer(9015, "Berserker", "berserker", "adc", "https://cdn.pandascore.co/images/player/image/9015/berserker.png", null),
            new PandaScorePlayer(9016, "Jojopyun", "jojopyun", "mid", "https://cdn.pandascore.co/images/player/image/9016/jojopyun.png", null)
        ));

        var tl = new PandaScoreTeam(1007, "Team Liquid", "team-liquid", "https://cdn.pandascore.co/images/team/image/1007/tl.png", List.of(
            new PandaScorePlayer(9017, "APA", "apa", "mid", "https://cdn.pandascore.co/images/player/image/9017/apa.png", null),
            new PandaScorePlayer(9018, "UmTi", "umti", "jungle", "https://cdn.pandascore.co/images/player/image/9018/umti.png", null)
        ));

        var blg = new PandaScoreTeam(1008, "Bilibili Gaming", "bilibili-gaming", "https://cdn.pandascore.co/images/team/image/1008/blg.png", List.of(
            new PandaScorePlayer(9019, "knight", "knight", "mid", "https://cdn.pandascore.co/images/player/image/9019/knight.png", null),
            new PandaScorePlayer(9020, "Bin", "bin", "top", "https://cdn.pandascore.co/images/player/image/9020/bin.png", null),
            new PandaScorePlayer(9021, "Elk", "elk", "adc", "https://cdn.pandascore.co/images/player/image/9021/elk.png", null)
        ));

        var tes = new PandaScoreTeam(1009, "Top Esports", "top-esports", "https://cdn.pandascore.co/images/team/image/1009/tes.png", List.of(
            new PandaScorePlayer(9022, "Creme", "creme", "mid", "https://cdn.pandascore.co/images/player/image/9022/creme.png", null),
            new PandaScorePlayer(9023, "JackeyLove", "jackeylove", "adc", "https://cdn.pandascore.co/images/player/image/9023/jackeylove.png", null),
            new PandaScorePlayer(9024, "Tian", "tian", "jungle", "https://cdn.pandascore.co/images/player/image/9024/tian.png", null)
        ));

        lolTeams = List.of(t1, geng, hle, g2, fnc, c9, tl, blg, tes);
    }

    private void initCs2() {
        cs2Leagues = List.of(
            new PandaScoreLeague(201, "BLAST Premier", "blast-premier", "https://cdn.pandascore.co/images/league/image/201/blast.png"),
            new PandaScoreLeague(202, "ESL Pro League", "esl-pro-league", "https://cdn.pandascore.co/images/league/image/202/esl.png"),
            new PandaScoreLeague(203, "IEM", "iem", "https://cdn.pandascore.co/images/league/image/203/iem.png")
        );

        var navi = new PandaScoreTeam(2001, "Natus Vincere", "natus-vincere", "https://cdn.pandascore.co/images/team/image/2001/navi.png", List.of(
            new PandaScorePlayer(9101, "s1mple", "s1mple", "rifler", "https://cdn.pandascore.co/images/player/image/9101/s1mple.png", null),
            new PandaScorePlayer(9102, "electronic", "electronic", "rifler", "https://cdn.pandascore.co/images/player/image/9102/electronic.png", null),
            new PandaScorePlayer(9103, "b1t", "b1t", "rifler", "https://cdn.pandascore.co/images/player/image/9103/b1t.png", null)
        ));

        var vitality = new PandaScoreTeam(2002, "Team Vitality", "team-vitality", "https://cdn.pandascore.co/images/team/image/2002/vitality.png", List.of(
            new PandaScorePlayer(9104, "ZywOo", "zywoo", "rifler", "https://cdn.pandascore.co/images/player/image/9104/zywoo.png", null),
            new PandaScorePlayer(9105, "apEX", "apex", "rifler", "https://cdn.pandascore.co/images/player/image/9105/apex.png", null)
        ));

        var faze = new PandaScoreTeam(2003, "FaZe Clan", "faze-clan", "https://cdn.pandascore.co/images/team/image/2003/faze.png", List.of(
            new PandaScorePlayer(9106, "rain", "rain", "rifler", "https://cdn.pandascore.co/images/player/image/9106/rain.png", null),
            new PandaScorePlayer(9107, "broky", "broky", "rifler", "https://cdn.pandascore.co/images/player/image/9107/broky.png", null)
        ));

        var g2cs = new PandaScoreTeam(2004, "G2 Esports", "g2-cs2", "https://cdn.pandascore.co/images/team/image/2004/g2cs.png", List.of(
            new PandaScorePlayer(9108, "NiKo", "niko", "rifler", "https://cdn.pandascore.co/images/player/image/9108/niko.png", null),
            new PandaScorePlayer(9109, "huNter-", "hunter", "rifler", "https://cdn.pandascore.co/images/player/image/9109/hunter.png", null)
        ));

        var heroic = new PandaScoreTeam(2005, "Heroic", "heroic", "https://cdn.pandascore.co/images/team/image/2005/heroic.png", List.of(
            new PandaScorePlayer(9110, "TeSeS", "teses", "rifler", "https://cdn.pandascore.co/images/player/image/9110/teses.png", null),
            new PandaScorePlayer(9111, "sjuush", "sjuush", "rifler", "https://cdn.pandascore.co/images/player/image/9111/sjuush.png", null)
        ));

        var mouz = new PandaScoreTeam(2006, "MOUZ", "mouz", "https://cdn.pandascore.co/images/team/image/2006/mouz.png", List.of(
            new PandaScorePlayer(9112, "torzsi", "torzsi", "rifler", "https://cdn.pandascore.co/images/player/image/9112/torzsi.png", null),
            new PandaScorePlayer(9113, "xertioN", "xertion", "rifler", "https://cdn.pandascore.co/images/player/image/9113/xertion.png", null)
        ));

        var spirit = new PandaScoreTeam(2007, "Team Spirit", "team-spirit", "https://cdn.pandascore.co/images/team/image/2007/spirit.png", List.of(
            new PandaScorePlayer(9114, "donk", "donk", "rifler", "https://cdn.pandascore.co/images/player/image/9114/donk.png", null),
            new PandaScorePlayer(9115, "sh1ro", "sh1ro", "rifler", "https://cdn.pandascore.co/images/player/image/9115/sh1ro.png", null),
            new PandaScorePlayer(9116, "chopper", "chopper", "rifler", "https://cdn.pandascore.co/images/player/image/9116/chopper.png", null)
        ));

        cs2Teams = List.of(navi, vitality, faze, g2cs, heroic, mouz, spirit);
    }

    @Override
    public List<PandaScoreMatch> fetchLolUpcoming() {
        Instant now = Instant.now();
        return List.of(
            makeMatch(800001, "HLE vs Gen.G", now.plus(1, ChronoUnit.DAYS), 3, 101, 1003, 1002),
            makeMatch(800002, "G2 vs Fnatic", now.plus(3, ChronoUnit.DAYS), 3, 102, 1004, 1005),
            makeMatch(800003, "Cloud9 vs Team Liquid", now.plus(5, ChronoUnit.DAYS), 3, 103, 1006, 1007),
            makeMatch(800004, "Bilibili Gaming vs Top Esports", now.plus(7, ChronoUnit.DAYS), 5, 104, 1008, 1009)
        );
    }

    @Override
    public List<PandaScoreMatch> fetchLolRunning() {
        return List.of();
    }

    @Override
    public List<PandaScoreMatch> fetchLolPast() {
        Instant now = Instant.now();
        return List.of(
            makeFinishedMatch(800005, "T1 vs Gen.G", now.minus(2, ChronoUnit.DAYS), 3, 101, 1001, 1002, 1001, List.of(
                new PandaScoreResult(1001, 2), new PandaScoreResult(1002, 1)
            )),
            makeFinishedMatch(800006, "G2 vs Fnatic", now.minus(4, ChronoUnit.DAYS), 3, 102, 1004, 1005, 1004, List.of(
                new PandaScoreResult(1004, 2), new PandaScoreResult(1005, 0)
            ))
        );
    }

    @Override
    public List<PandaScoreLeague> fetchLolLeagues() {
        return lolLeagues;
    }

    @Override
    public List<PandaScoreTeam> fetchLolTeams() {
        return lolTeams;
    }

    @Override
    public List<PandaScoreMatch> fetchCs2Upcoming() {
        Instant now = Instant.now();
        return List.of(
            makeMatch(801001, "NaVi vs Team Vitality", now.plus(2, ChronoUnit.DAYS), 3, 201, 2001, 2002),
            makeMatch(801002, "FaZe vs G2 Esports", now.plus(4, ChronoUnit.DAYS), 3, 202, 2003, 2004),
            makeMatch(801003, "Heroic vs MOUZ", now.plus(6, ChronoUnit.DAYS), 3, 203, 2005, 2006)
        );
    }

    @Override
    public List<PandaScoreMatch> fetchCs2Running() {
        return List.of();
    }

    @Override
    public List<PandaScoreMatch> fetchCs2Past() {
        Instant now = Instant.now();
        return List.of(
            makeFinishedMatch(801004, "NaVi vs FaZe", now.minus(2, ChronoUnit.DAYS), 3, 201, 2001, 2003, 2001, List.of(
                new PandaScoreResult(2001, 2), new PandaScoreResult(2003, 1)
            )),
            makeFinishedMatch(801005, "Vitality vs Heroic", now.minus(5, ChronoUnit.DAYS), 3, 201, 2002, 2005, 2002, List.of(
                new PandaScoreResult(2002, 2), new PandaScoreResult(2005, 0)
            ))
        );
    }

    @Override
    public List<PandaScoreMatch> fetchPastMatches(String game, int page, int perPage) {
        return List.of();
    }

    @Override
    public List<PandaScoreLeague> fetchCs2Leagues() {
        return cs2Leagues;
    }

    @Override
    public List<PandaScoreTeam> fetchCs2Teams() {
        return cs2Teams;
    }

    private PandaScoreMatch makeMatch(long id, String name, Instant start, int games,
                                       long leagueId, long teamAId, long teamBId) {
        String slug = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        var leagueRef = findLeagueRef(leagueId);
        return new PandaScoreMatch(
            id, name, slug, "not_started", start.toString(), null, null,
            games, "best_of",
            List.of(new PandaScoreOpponent(new PandaScoreTeamRef(teamAId, teamName(teamAId), null, null), "Team"),
                    new PandaScoreOpponent(new PandaScoreTeamRef(teamBId, teamName(teamBId), null, null), "Team")),
            null, null, null, leagueRef,
            new PandaScoreTournamentRef(leagueId * 50 + 1, leagueRef.name() + " Summer 2026", "summer-2026")
        );
    }

    private PandaScoreMatch makeFinishedMatch(long id, String name, Instant start, int games,
                                               long leagueId, long teamAId, long teamBId,
                                               long winnerId, List<PandaScoreResult> results) {
        var m = makeMatch(id, name, start, games, leagueId, teamAId, teamBId);
        return new PandaScoreMatch(
            m.id(), m.name(), m.slug(), "finished", m.scheduledAt(), start.toString(), start.plus(3, ChronoUnit.HOURS).toString(),
            m.numberOfGames(), m.matchType(), m.opponents(), results,
            null, winnerId, m.league(), m.tournament()
        );
    }

    private PandaScoreLeagueRef findLeagueRef(long leagueId) {
        for (var league : lolLeagues) {
            if (league.id() == leagueId) return new PandaScoreLeagueRef(league.id(), league.name(), league.slug(), league.imageUrl());
        }
        for (var league : cs2Leagues) {
            if (league.id() == leagueId) return new PandaScoreLeagueRef(league.id(), league.name(), league.slug(), league.imageUrl());
        }
        return new PandaScoreLeagueRef(leagueId, "Unknown", "unknown", null);
    }

    private String teamName(long teamId) {
        for (var team : lolTeams) {
            if (team.id() == teamId) return team.name();
        }
        for (var team : cs2Teams) {
            if (team.id() == teamId) return team.name();
        }
        return "Team " + teamId;
    }
}
