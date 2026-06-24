package com.predicto.betting;

import com.predicto.auth.User;
import com.predicto.catalog.Match;
import com.predicto.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "score_odds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreOdds extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "score_value", nullable = false)
    private String scoreValue;

    @Column(name = "odds_value", nullable = false, precision = 5, scale = 2)
    private BigDecimal oddsValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_by_user_id", nullable = false)
    private User setByUser;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
