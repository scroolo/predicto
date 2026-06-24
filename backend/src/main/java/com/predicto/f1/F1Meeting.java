package com.predicto.f1;

import com.predicto.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "f1_meetings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class F1Meeting extends BaseEntity {

    @Column(name = "meeting_key", nullable = false, unique = true)
    private Integer meetingKey;

    @Column(name = "meeting_name", nullable = false)
    private String meetingName;

    @Column(name = "meeting_official_name")
    private String meetingOfficialName;

    @Column(name = "country_name")
    private String countryName;

    @Column(name = "country_flag_url")
    private String countryFlagUrl;

    @Column(name = "circuit_short_name")
    private String circuitShortName;

    @Column(name = "circuit_image_url")
    private String circuitImageUrl;

    @Column(name = "location")
    private String location;

    @Column(name = "date_start", nullable = false)
    private OffsetDateTime dateStart;

    @Column(name = "date_end")
    private OffsetDateTime dateEnd;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "is_cancelled", nullable = false)
    @Builder.Default
    private Boolean isCancelled = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
