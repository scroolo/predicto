package com.predicto.catalog;

import com.predicto.common.BaseEntity;
import com.predicto.common.enums.Game;
import com.predicto.common.enums.MatchFormat;
import com.predicto.common.enums.MatchStatus;
import com.predicto.common.enums.Source;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match extends BaseEntity {

    @Column(name = "external_id", unique = true)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Source source = Source.MANUAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_a_id", nullable = false)
    private Team teamA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_b_id", nullable = false)
    private Team teamB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchFormat format;

    private String stage;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MatchStatus status = MatchStatus.SCHEDULED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_winner_team_id")
    private Team resultWinnerTeam;

    @Column(name = "result_score")
    private String resultScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_mvp_player_id")
    private Player resultMvpPlayer;

    @Column(name = "locked_at")
    private OffsetDateTime lockedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
