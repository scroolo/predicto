package com.predicto.achievement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_achievements")
@Getter @Setter
public class UserAchievement {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "achievement_id")
    private Achievement achievement;

    private LocalDateTime unlockedAt;

    @PrePersist
    public void prePersist() {
        unlockedAt = LocalDateTime.now();
    }
}
