package com.predicto.season;

import com.predicto.PredictoApplication;
import com.predicto.auth.User;
import com.predicto.auth.UserRepository;
import com.predicto.auth.security.JwtUtil;
import com.predicto.betting.*;
import com.predicto.catalog.*;
import com.predicto.common.enums.*;
import com.predicto.season.dto.CreateSeasonRequest;
import com.predicto.season.dto.LeaderboardEntryResponse;
import com.predicto.season.dto.RewardResponse;
import com.predicto.season.dto.SeasonResponse;
import com.predicto.wallet.Wallet;
import com.predicto.wallet.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = PredictoApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@Testcontainers
class SeasonIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("predicto-test")
            .withUsername("predicto")
            .withPassword("predicto");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private TestRestTemplate rest;
    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private LeagueRepository leagueRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private MatchRepository matchRepository;
    @Autowired private BetRepository betRepository;
    @Autowired private MatchOddsRepository matchOddsRepository;
    @Autowired private ScoreOddsRepository scoreOddsRepository;
    @Autowired private SeasonRepository seasonRepository;
    @Autowired private LeaderboardEntryRepository leaderboardEntryRepository;
    @Autowired private RewardRepository rewardRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private LeaderboardService leaderboardService;
    @Autowired private SettlementService settlementService;
    @Autowired private OddsService oddsService;

    private User admin;
    private User userA;
    private User userB;
    private League league;
    private Team teamA;
    private Team teamB;
    private Team teamC;
    private Team teamD;
    private Match match1;
    private Match match2;
    private String adminToken;
    private String userAToken;
    private String userBToken;
    private Season season;

    @BeforeEach
    void setUp() {
        rewardRepository.deleteAll();
        leaderboardEntryRepository.deleteAll();
        betRepository.deleteAll();
        matchOddsRepository.deleteAll();
        scoreOddsRepository.deleteAll();
        matchRepository.deleteAll();
        playerRepository.deleteAll();
        teamRepository.deleteAll();
        leagueRepository.deleteAll();
        seasonRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        admin = userRepository.save(User.builder()
                .username("admin-" + UUID.randomUUID().toString().substring(0, 6))
                .displayName("Admin")
                .role(UserRole.ADMIN)
                .build());
        walletRepository.save(Wallet.builder().user(admin).balance(10000).build());

        userA = userRepository.save(User.builder()
                .username("usera-" + UUID.randomUUID().toString().substring(0, 6))
                .displayName("Player A")
                .role(UserRole.USER)
                .build());
        walletRepository.save(Wallet.builder().user(userA).balance(10000).build());

        userB = userRepository.save(User.builder()
                .username("userb-" + UUID.randomUUID().toString().substring(0, 6))
                .displayName("Player B")
                .role(UserRole.USER)
                .build());
        walletRepository.save(Wallet.builder().user(userB).balance(10000).build());

        adminToken = jwtUtil.generateToken(admin.getId(), admin.getUsername(), admin.getRole().name());
        userAToken = jwtUtil.generateToken(userA.getId(), userA.getUsername(), userA.getRole().name());
        userBToken = jwtUtil.generateToken(userB.getId(), userB.getUsername(), userB.getRole().name());

        league = leagueRepository.save(League.builder()
                .name("Test League")
                .game(Game.LOL)
                .build());

        teamA = teamRepository.save(Team.builder().name("Team Alpha").game(Game.LOL).league(league).build());
        teamB = teamRepository.save(Team.builder().name("Team Beta").game(Game.LOL).league(league).build());
        teamC = teamRepository.save(Team.builder().name("Team Gamma").game(Game.LOL).league(league).build());
        teamD = teamRepository.save(Team.builder().name("Team Delta").game(Game.LOL).league(league).build());

        season = seasonRepository.save(Season.builder()
                .name("Test Season")
                .game(Game.LOL)
                .type(SeasonType.MONTHLY)
                .startsAt(OffsetDateTime.now().minusDays(1))
                .endsAt(OffsetDateTime.now().plusDays(30))
                .status(SeasonStatus.ACTIVE)
                .build());

        match1 = matchRepository.save(Match.builder()
                .game(Game.LOL).league(league).teamA(teamA).teamB(teamB)
                .format(MatchFormat.BO3)
                .startsAt(OffsetDateTime.now().plusHours(2))
                .status(MatchStatus.SCHEDULED)
                .source(Source.MANUAL)
                .build());

        match2 = matchRepository.save(Match.builder()
                .game(Game.LOL).league(league).teamA(teamC).teamB(teamD)
                .format(MatchFormat.BO3)
                .startsAt(OffsetDateTime.now().plusHours(3))
                .status(MatchStatus.SCHEDULED)
                .source(Source.MANUAL)
                .build());
    }

    private HttpEntity<?> authRequest(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<?> authRequest(String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private void setOdds(Match m) {
        var body = Map.of(
                "winnerOdds", List.of(
                        Map.of("teamId", m.getTeamA().getId().toString(), "oddsValue", 2.0),
                        Map.of("teamId", m.getTeamB().getId().toString(), "oddsValue", 2.0)
                ),
                "scoreOdds", List.of(
                        Map.of("scoreValue", "2:0", "oddsValue", 2.1),
                        Map.of("scoreValue", "2:1", "oddsValue", 3.0),
                        Map.of("scoreValue", "0:2", "oddsValue", 2.1),
                        Map.of("scoreValue", "1:2", "oddsValue", 3.0)
                )
        );
        var resp = rest.exchange("/api/admin/matches/" + m.getId() + "/odds",
                HttpMethod.PUT, authRequest(adminToken, body), Map.class);
        assertEquals(200, resp.getStatusCode().value(),
                "Failed to set odds for match " + m.getId() + ": " + resp.getBody());
    }

    private void placeBet(UUID matchId, UUID teamId, int stake, String token) {
        var body = Map.of("winnerTeamId", teamId.toString(), "stake", stake);
        var resp = rest.exchange("/api/matches/" + matchId + "/bets",
                HttpMethod.POST, authRequest(token, body), Map.class);
        assertEquals(200, resp.getStatusCode().value(),
                "Failed to place bet on match " + matchId + ": " + resp.getBody());
    }

    // --- TESTS ---

    @Test
    void placingBetsLinksToActiveSeason() {
        setOdds(match1);
        setOdds(match2);

        placeBet(match1.getId(), teamA.getId(), 200, userAToken);
        placeBet(match2.getId(), teamC.getId(), 150, userAToken);

        List<Bet> userABets = betRepository.findByUserId(userA.getId());
        assertEquals(2, userABets.size());
        for (Bet bet : userABets) {
            assertNotNull(bet.getSeason(), "Bet should be linked to active season, but season was null");
            assertEquals(season.getId(), bet.getSeason().getId());
        }
    }

    @Test
    void recomputeCorrectlyScoresAndRanksTwoUsers() {
        setOdds(match1);
        setOdds(match2);

        // userA bets on teamA (match1), userB bets on teamB (match1)
        placeBet(match1.getId(), teamA.getId(), 200, userAToken);
        placeBet(match1.getId(), teamB.getId(), 200, userBToken);

        // Settle match1: teamA wins via service directly
        match1.setStatus(MatchStatus.LOCKED);
        matchRepository.save(match1);
        match1.setResultWinnerTeam(teamA);
        match1.setResultScore("2:0");
        match1.setStatus(MatchStatus.FINISHED);
        match1.setResultConfirmedAt(OffsetDateTime.now());
        matchRepository.save(match1);
        settlementService.settleMatch(match1);
        // Settlement triggers recompute via LeaderboardService

        var lbResp = rest.exchange("/api/seasons/" + season.getId() + "/leaderboard",
                HttpMethod.GET, null, LeaderboardEntryResponse[].class);
        assertEquals(200, lbResp.getStatusCode().value());

        var entries = List.of(lbResp.getBody());
        assertEquals(2, entries.size());

        var entryA = entries.stream().filter(e -> e.userId().equals(userA.getId())).findFirst().orElseThrow();
        var entryB = entries.stream().filter(e -> e.userId().equals(userB.getId())).findFirst().orElseThrow();
        assertEquals(1, (int) entryA.points());
        assertEquals(0, (int) entryB.points());
        assertTrue(entryA.rankPosition() < entryB.rankPosition());
    }

    @Test
    void tiebreakerUsesCorrectPicks() {
        setOdds(match1);
        setOdds(match2);

        placeBet(match1.getId(), teamA.getId(), 200, userAToken);
        placeBet(match1.getId(), teamA.getId(), 200, userBToken);

        placeBet(match2.getId(), teamD.getId(), 100, userAToken);
        placeBet(match2.getId(), teamC.getId(), 100, userBToken);

        // Settle match1: teamA wins
        match1.setStatus(MatchStatus.LOCKED);
        matchRepository.save(match1);
        match1.setResultWinnerTeam(teamA);
        match1.setResultScore("2:0");
        match1.setStatus(MatchStatus.FINISHED);
        match1.setResultConfirmedAt(OffsetDateTime.now());
        matchRepository.save(match1);
        settlementService.settleMatch(match1);

        // Settle match2: teamD wins
        match2.setStatus(MatchStatus.LOCKED);
        matchRepository.save(match2);
        match2.setResultWinnerTeam(teamD);
        match2.setResultScore("2:1");
        match2.setStatus(MatchStatus.FINISHED);
        match2.setResultConfirmedAt(OffsetDateTime.now());
        matchRepository.save(match2);
        settlementService.settleMatch(match2);

        var lbResp = rest.exchange("/api/seasons/" + season.getId() + "/leaderboard",
                HttpMethod.GET, null, LeaderboardEntryResponse[].class);
        var entries = List.of(lbResp.getBody());

        var entryA = entries.stream().filter(e -> e.userId().equals(userA.getId())).findFirst().orElseThrow();
        var entryB = entries.stream().filter(e -> e.userId().equals(userB.getId())).findFirst().orElseThrow();
        assertEquals(2, (int) entryA.points());
        assertEquals(2, (int) entryA.points());
        assertEquals(1, (int) entryB.points());
        assertEquals(2, (int) entryA.correctPicks());
        assertEquals(1, (int) entryB.correctPicks());
        assertTrue(entryA.rankPosition() < entryB.rankPosition(),
                "userA should be ranked higher due to tiebreaker");
    }

    @Test
    void computeAndFinaliseCreatesCorrectNumberOfRewards() {
        setOdds(match1);
        setOdds(match2);

        placeBet(match1.getId(), teamA.getId(), 200, userAToken);
        placeBet(match2.getId(), teamC.getId(), 100, userAToken);

        match1.setStatus(MatchStatus.LOCKED);
        matchRepository.save(match1);
        match1.setResultWinnerTeam(teamA);
        match1.setResultScore("2:0");
        match1.setStatus(MatchStatus.FINISHED);
        match1.setResultConfirmedAt(OffsetDateTime.now());
        matchRepository.save(match1);
        settlementService.settleMatch(match1);

        match2.setStatus(MatchStatus.LOCKED);
        matchRepository.save(match2);
        match2.setResultWinnerTeam(teamC);
        match2.setResultScore("2:0");
        match2.setStatus(MatchStatus.FINISHED);
        match2.setResultConfirmedAt(OffsetDateTime.now());
        matchRepository.save(match2);
        settlementService.settleMatch(match2);

        // Close the season via service directly
        season.setStatus(SeasonStatus.CLOSED);
        seasonRepository.save(season);
        leaderboardService.computeAndFinalise(season.getId());

        var rewardsResp = rest.exchange("/api/seasons/" + season.getId() + "/rewards",
                HttpMethod.GET, null, RewardResponse[].class);
        var rewards = List.of(rewardsResp.getBody());
        assertEquals(1, rewards.size());
        assertEquals(1, (int) rewards.get(0).rankPosition());
        assertEquals(userA.getId(), rewards.get(0).userId());
        assertFalse(rewards.get(0).claimed());
    }

    @Test
    void closeSeasonWithFewerThanFiveParticipants() {
        setOdds(match1);
        placeBet(match1.getId(), teamA.getId(), 200, userAToken);

        match1.setStatus(MatchStatus.LOCKED);
        matchRepository.save(match1);
        match1.setResultWinnerTeam(teamA);
        match1.setResultScore("2:0");
        match1.setStatus(MatchStatus.FINISHED);
        match1.setResultConfirmedAt(OffsetDateTime.now());
        matchRepository.save(match1);
        settlementService.settleMatch(match1);

        // Close via service
        leaderboardService.computeAndFinalise(season.getId());

        var rewardsResp = rest.exchange("/api/seasons/" + season.getId() + "/rewards",
                HttpMethod.GET, null, RewardResponse[].class);
        var rewards = List.of(rewardsResp.getBody());
        assertEquals(1, rewards.size());
        assertEquals(1, (int) rewards.get(0).rankPosition());
    }

    @Test
    void publicEndpointsSucceed() {
        var seasonsResp = rest.exchange("/api/seasons", HttpMethod.GET, null, SeasonResponse[].class);
        assertEquals(200, seasonsResp.getStatusCode().value());

        var activeResp = rest.exchange("/api/seasons/active", HttpMethod.GET, null, SeasonResponse[].class);
        assertEquals(200, activeResp.getStatusCode().value());

        var lbResp = rest.exchange("/api/seasons/" + season.getId() + "/leaderboard",
                HttpMethod.GET, null, LeaderboardEntryResponse[].class);
        assertEquals(200, lbResp.getStatusCode().value());
    }

    @Test
    void seasonAdminCreateFailsValidation() {
        // Create season via API — test that invalid dates are rejected
        var invalidBody = Map.of(
                "name", "Bad Season",
                "game", "LOL",
                "type", "MONTHLY",
                "startsAt", OffsetDateTime.now().plusDays(10).toString(),
                "endsAt", OffsetDateTime.now().plusDays(5).toString()
        );
        var resp = rest.exchange("/api/admin/seasons",
                HttpMethod.POST, authRequest(adminToken, invalidBody), Map.class);
        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    void placingBetWithoutActiveSeasonLeavesSeasonNull() {
        season.setStatus(SeasonStatus.UPCOMING);
        seasonRepository.save(season);

        setOdds(match1);
        placeBet(match1.getId(), teamA.getId(), 200, userAToken);

        var bet = betRepository.findByUserIdAndMatchId(userA.getId(), match1.getId()).orElseThrow();
        assertNull(bet.getSeason());
    }
}
