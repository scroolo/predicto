package com.predicto.season;

import com.predicto.auth.User;
import com.predicto.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "leaderboard_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer points;

    @Column(name = "correct_picks", nullable = false)
    private Integer correctPicks;

    @Column(name = "mvp_correct", nullable = false)
    private Integer mvpCorrect;

    @Column(name = "score_correct", nullable = false)
    private Integer scoreCorrect;

    @Column(name = "rank_position")
    private Integer rankPosition;

    @Column(name = "computed_at", nullable = false)
    private OffsetDateTime computedAt;
}
