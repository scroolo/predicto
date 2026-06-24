package com.predicto.catalog;

import com.predicto.common.BaseEntity;
import com.predicto.common.enums.Source;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player extends BaseEntity {

    @Column(name = "external_id")
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Source source = Source.MANUAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    private String nickname;

    private String role;

    @Column(name = "photo_url")
    private String photoUrl;
}
