package com.predicto.betting;

import com.predicto.PredictoApplication;
import com.predicto.auth.User;
import com.predicto.auth.UserRepository;
import com.predicto.auth.security.JwtUtil;
import com.predicto.catalog.*;
import com.predicto.common.enums.*;
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

import com.predicto.betting.dto.BetResponse;
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
class BettingIntegrationTest {

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

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private MatchOddsRepository matchOddsRepository;

    @Autowired
    private ScoreOddsRepository scoreOddsRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private User admin;
    private User user;
    private Match match;
    private Team teamA;
    private Team teamB;
    private Player mvpPlayer;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        scoreOddsRepository.deleteAll();
        matchOddsRepository.deleteAll();
        betRepository.deleteAll();
        matchRepository.deleteAll();
        playerRepository.deleteAll();
        teamRepository.deleteAll();
        leagueRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        admin = userRepository.save(User.builder()
                .username("admin-" + UUID.randomUUID().toString().substring(0, 6))
                .displayName("Admin")
                .role(UserRole.ADMIN)
                .build());
        walletRepository.save(Wallet.builder().user(admin).balance(10000).build());

        user = userRepository.save(User.builder()
                .username("user-" + UUID.randomUUID().toString().substring(0, 6))
                .displayName("User")
                .role(UserRole.USER)
                .build());
        walletRepository.save(Wallet.builder().user(user).balance(1000).build());

        adminToken = jwtUtil.generateToken(admin.getId(), admin.getUsername(), admin.getRole().name());
        userToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        League league = leagueRepository.save(League.builder()
                .name("Test League")
                .game(Game.LOL)
                .build());

        teamA = teamRepository.save(Team.builder()
                .name("Team Alpha")
                .game(Game.LOL)
                .league(league)
                .build());

        teamB = teamRepository.save(Team.builder()
                .name("Team Beta")
                .game(Game.LOL)
                .league(league)
                .build());

        mvpPlayer = playerRepository.save(Player.builder()
                .nickname("PlayerOne")
                .team(teamA)
                .build());

        match = matchRepository.save(Match.builder()
                .game(Game.LOL)
                .league(league)
                .teamA(teamA)
                .teamB(teamB)
                .format(MatchFormat.BO3)
                .startsAt(OffsetDateTime.now().plusHours(1))
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> setOdds() {
        var body = Map.of(
                "winnerOdds", List.of(
                        Map.of("teamId", teamA.getId().toString(), "oddsValue", 2.0),
                        Map.of("teamId", teamB.getId().toString(), "oddsValue", 2.0)
                ),
                "scoreOdds", List.of(
                        Map.of("scoreValue", "2:0", "oddsValue", 2.1),
                        Map.of("scoreValue", "2:1", "oddsValue", 3.0),
                        Map.of("scoreValue", "0:2", "oddsValue", 2.1),
                        Map.of("scoreValue", "1:2", "oddsValue", 3.0)
                )
        );
        var resp = rest.exchange("/api/admin/matches/" + match.getId() + "/odds",
                HttpMethod.PUT, authRequest(adminToken, body), Map.class);
        if (resp.getStatusCode().isError()) {
            fail("Failed to set odds: " + resp.getBody());
        }
        return resp.getBody();
    }

    // ---- TESTS ----

    @Test
    void placeBetCorrectlyDebitsWalletAndIncreasesLifetimeWagered() {
        setOdds();

        var betBody = Map.of("winnerTeamId", teamA.getId().toString(), "stake", 200);
        var betResp = rest.exchange("/api/matches/" + match.getId() + "/bets",
                HttpMethod.POST, authRequest(userToken, betBody), Map.class);
        assertEquals(200, betResp.getStatusCode().value());

        var meResp = rest.exchange("/api/auth/me", HttpMethod.GET, authRequest(userToken), Map.class);
        Map<String, Object> walletData = (Map<String, Object>) ((Map<String, Object>) meResp.getBody()).get("wallet");
        assertEquals(800, walletData.get("balance"));
        assertEquals(200, walletData.get("lifetimeWageredLol"));
    }

    @Test
    void placeBetExceedingBalanceIsRejected() {
        setOdds();

        var betBody = Map.of("winnerTeamId", teamA.getId().toString(), "stake", 999999);
        var betResp = rest.exchange("/api/matches/" + match.getId() + "/bets",
                HttpMethod.POST, authRequest(userToken, betBody), Map.class);
        assertEquals(400, betResp.getStatusCode().value());
    }

    @Test
    void placeBetOnLockedMatchIsRejected() {
        match.setStatus(MatchStatus.LOCKED);
        matchRepository.save(match);

        var betBody = Map.of("winnerTeamId", teamA.getId().toString(), "stake", 100);
        var betResp = rest.exchange("/api/matches/" + match.getId() + "/bets",
                HttpMethod.POST, authRequest(userToken, betBody), Map.class);
        assertEquals(409, betResp.getStatusCode().value());
    }

    @Test
    void replaceBetNetsOutCorrectly() {
        setOdds();

        // First bet
        var bet1 = Map.of("winnerTeamId", teamA.getId().toString(), "stake", 200);
        rest.exchange("/api/matches/" + match.getId() + "/bets",
                HttpMethod.POST, authRequest(userToken, bet1), Map.class);

        // Replace with higher stake
        var bet2 = Map.of("winnerTeamId", teamB.getId().toString(), "stake", 300);
        var resp2 = rest.exchange("/api/matches/" + match.getId() + "/bets",
                HttpMethod.POST, authRequest(userToken, bet2), Map.class);
        assertEquals(200, resp2.getStatusCode().value());

        var meResp = rest.exchange("/api/auth/me", HttpMethod.GET, authRequest(userToken), Map.class);
        Map<String, Object> walletData = (Map<String, Object>) ((Map<String, Object>) meResp.getBody()).get("wallet");
        assertEquals(700, walletData.get("balance"));  // 1000 - 300 (old 200 refunded)
        assertEquals(300, walletData.get("lifetimeWageredLol"));
    }

    @Test
    void settleMatchCorrectlyComputesPointsAndPayout() {
        setOdds();

        rest.exchange("/api/matches/" + match.getId() + "/bets",
                HttpMethod.POST, authRequest(userToken,
                        Map.of("winnerTeamId", teamA.getId().toString(), "stake", 200)),
                Map.class);

        match.setStatus(MatchStatus.LOCKED);
        matchRepository.save(match);

        var confirmResp = rest.exchange("/api/admin/matches/" + match.getId() + "/confirm-result",
                HttpMethod.POST, authRequest(adminToken,
                        Map.of("winnerTeamId", teamA.getId().toString(), "score", "2:0")),
                Map.class);
        assertEquals(200, confirmResp.getStatusCode().value());

        var betsResp = rest.exchange("/api/users/me/bets",
                HttpMethod.GET, authRequest(userToken), BetResponse[].class);
        BetResponse[] bets = betsResp.getBody();
        assertNotNull(bets);
        assertEquals(1, bets.length);
        assertEquals(1, bets[0].pointsAwarded());
        assertEquals(400, bets[0].actualReturn()); // floor(200 * 2.0)
        assertEquals(BetStatus.WON, bets[0].status());

        var meResp = rest.exchange("/api/auth/me", HttpMethod.GET, authRequest(userToken), Map.class);
        Map<String, Object> walletData = (Map<String, Object>) ((Map<String, Object>) meResp.getBody()).get("wallet");
        assertEquals(1200, walletData.get("balance")); // 1000 - 200 + 400
    }

    @Test
    void cancelMatchVoidsAndRefundsBets() {
        setOdds();

        rest.exchange("/api/matches/" + match.getId() + "/bets",
                HttpMethod.POST, authRequest(userToken,
                        Map.of("winnerTeamId", teamA.getId().toString(), "stake", 200)),
                Map.class);

        var cancelResp = rest.exchange("/api/admin/matches/" + match.getId() + "/cancel",
                HttpMethod.POST, authRequest(adminToken), Map.class);
        assertEquals(200, cancelResp.getStatusCode().value());

        var betsResp = rest.exchange("/api/users/me/bets",
                HttpMethod.GET, authRequest(userToken), BetResponse[].class);
        BetResponse[] bets = betsResp.getBody();
        assertNotNull(bets);
        assertEquals(1, bets.length);
        assertEquals(BetStatus.VOID, bets[0].status());
        assertEquals(0, bets[0].actualReturn());

        var meResp = rest.exchange("/api/auth/me", HttpMethod.GET, authRequest(userToken), Map.class);
        Map<String, Object> walletData = (Map<String, Object>) ((Map<String, Object>) meResp.getBody()).get("wallet");
        assertEquals(1000, walletData.get("balance"));
        assertEquals(0, walletData.get("lifetimeWageredLol"));
    }

    @Test
    void partiallyCorrectBetScoresCorrectly() {
        setOdds();

        rest.exchange("/api/matches/" + match.getId() + "/bets",
                HttpMethod.POST, authRequest(userToken,
                        Map.of("winnerTeamId", teamA.getId().toString(), "stake", 200,
                                "mvpPlayerId", mvpPlayer.getId().toString(),
                                "exactScore", "2:0", "scoreStake", 100)),
                Map.class);

        match.setStatus(MatchStatus.LOCKED);
        matchRepository.save(match);

        // Confirm: Team A wins 2:1 (winner correct, score wrong, no MVP)
        rest.exchange("/api/admin/matches/" + match.getId() + "/confirm-result",
                HttpMethod.POST, authRequest(adminToken,
                        Map.of("winnerTeamId", teamA.getId().toString(), "score", "2:1")),
                Map.class);

        var betsResp = rest.exchange("/api/users/me/bets",
                HttpMethod.GET, authRequest(userToken), BetResponse[].class);
        BetResponse[] bets = betsResp.getBody();
        assertNotNull(bets);
        assertEquals(1, bets.length);

        // Winner correct: 1 point + floor(200 * 2.0) = 400
        // Score wrong: 0 points
        // MVP null in confirm: 0 points
        assertEquals(1, bets[0].pointsAwarded());
        assertEquals(400, bets[0].actualReturn());
        assertEquals(BetStatus.WON, bets[0].status());
    }

    @Test
    void fullyWrongBetGetsLostStatus() {
        setOdds();

        rest.exchange("/api/matches/" + match.getId() + "/bets",
                HttpMethod.POST, authRequest(userToken,
                        Map.of("winnerTeamId", teamA.getId().toString(), "stake", 200)),
                Map.class);

        match.setStatus(MatchStatus.LOCKED);
        matchRepository.save(match);

        // Team B wins
        rest.exchange("/api/admin/matches/" + match.getId() + "/confirm-result",
                HttpMethod.POST, authRequest(adminToken,
                        Map.of("winnerTeamId", teamB.getId().toString(), "score", "2:0")),
                Map.class);

        var betsResp = rest.exchange("/api/users/me/bets",
                HttpMethod.GET, authRequest(userToken), BetResponse[].class);
        BetResponse[] bets = betsResp.getBody();
        assertNotNull(bets);
        assertEquals(1, bets.length);
        assertEquals(0, bets[0].pointsAwarded());
        assertEquals(0, bets[0].actualReturn());
        assertEquals(BetStatus.LOST, bets[0].status());
    }

    @Test
    void cancelBetByUserRefundsCorrectly() {
        setOdds();

        rest.exchange("/api/matches/" + match.getId() + "/bets",
                HttpMethod.POST, authRequest(userToken,
                        Map.of("winnerTeamId", teamA.getId().toString(), "stake", 200)),
                Map.class);

        var cancelResp = rest.exchange("/api/matches/" + match.getId() + "/bets",
                HttpMethod.DELETE, authRequest(userToken), Map.class);
        assertEquals(200, cancelResp.getStatusCode().value());

        var meResp = rest.exchange("/api/auth/me", HttpMethod.GET, authRequest(userToken), Map.class);
        Map<String, Object> walletData = (Map<String, Object>) ((Map<String, Object>) meResp.getBody()).get("wallet");
        assertEquals(1000, walletData.get("balance")); // fully refunded
        assertEquals(0, walletData.get("lifetimeWageredLol"));
    }
}
