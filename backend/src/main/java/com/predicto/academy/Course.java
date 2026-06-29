package com.predicto.academy;

import com.predicto.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcademyCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcademyLevel level;

    @Column(nullable = false)
    @Builder.Default
    private Integer xpReward = 200;

    @Column(nullable = false)
    @Builder.Default
    private Boolean published = false;

    @Column
    private String imageUrl;
}
