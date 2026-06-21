package com.predicto.pickban;

import com.predicto.auth.User;
import com.predicto.catalog.League;
import com.predicto.common.BaseEntity;
import com.predicto.season.Season;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "pickban_predictions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickBanPrediction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private Season season;

    @Column(name = "most_picked_champion")
    private String mostPickedChampion;

    @Column(name = "most_banned_champion")
    private String mostBannedChampion;

    @Column(name = "most_kills_champion")
    private String mostKillsChampion;

    @Column(name = "most_assists_champion")
    private String mostAssistsChampion;

    @Column(name = "pentakill_champion")
    private String pentakillChampion;

    @Builder.Default
    private Boolean locked = false;

    @Column(name = "points_awarded")
    @Builder.Default
    private Integer pointsAwarded = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
