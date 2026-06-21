package com.predicto.season;

import com.predicto.common.BaseEntity;
import com.predicto.common.enums.Game;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rank_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankTier extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Game game;

    @Column(name = "tier_name", nullable = false)
    private String tierName;

    @Column(name = "min_wagered", nullable = false)
    private Integer minWagered;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
