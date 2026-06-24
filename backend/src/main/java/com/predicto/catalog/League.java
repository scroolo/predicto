package com.predicto.catalog;

import com.predicto.common.BaseEntity;
import com.predicto.common.enums.Game;
import com.predicto.common.enums.Source;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leagues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class League extends BaseEntity {

    @Column(name = "external_id")
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Source source = Source.MANUAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Game game;

    @Column(nullable = false)
    private String name;

    private String region;

    @Column(name = "logo_url")
    private String logoUrl;
}
