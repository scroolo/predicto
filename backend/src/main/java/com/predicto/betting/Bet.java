package com.predicto.betting;

import com.predicto.auth.User;
import com.predicto.catalog.Match;
import com.predicto.catalog.Player;
import com.predicto.catalog.Team;
import com.predicto.common.BaseEntity;
import com.predicto.common.enums.BetStatus;
import com.predicto.season.Season;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_team_id")
    private Team winnerTeam;

    @Column(name = "winner_odds_snapshot", precision = 5, scale = 2)
    private BigDecimal winnerOddsSnapshot;

    @Column(nullable = false)
    private Integer stake;

    @Column(name = "potential_return", nullable = false)
    private Integer potentialReturn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mvp_player_id")
    private Player mvpPlayer;

    @Column(name = "exact_score")
    private String exactScore;

    @Column(name = "score_odds_snapshot", precision = 5, scale = 2)
    private BigDecimal scoreOddsSnapshot;

    @Column(name = "score_stake")
    private Integer scoreStake;

    @Column(name = "score_potential_return")
    private Integer scorePotentialReturn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BetStatus status = BetStatus.PENDING;

    @Column(name = "points_awarded")
    @Builder.Default
    private Integer pointsAwarded = 0;

    @Column(name = "actual_return")
    @Builder.Default
    private Integer actualReturn = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "settled_at")
    private OffsetDateTime settledAt;
}
