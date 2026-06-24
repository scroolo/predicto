package com.predicto.catalog;

import com.predicto.common.BaseEntity;
import com.predicto.common.enums.Game;
import com.predicto.common.enums.Source;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team extends BaseEntity {

    @Column(name = "external_id")
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Source source = Source.MANUAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    private League league;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Game game;

    @Column(nullable = false)
    private String name;

    @Column(name = "short_code")
    private String shortCode;

    @Column(name = "logo_url")
    private String logoUrl;

    private String color;
}
