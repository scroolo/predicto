package com.predicto.season;

import com.predicto.common.BaseEntity;
import com.predicto.common.enums.Game;
import com.predicto.common.enums.SeasonStatus;
import com.predicto.common.enums.SeasonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "seasons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Season extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeasonType type;

    @Column(nullable = false)
    private String name;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private OffsetDateTime endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SeasonStatus status = SeasonStatus.UPCOMING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
