package com.predicto.f1;

import com.predicto.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "f1_drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class F1Driver extends BaseEntity {

    @Column(name = "driver_number", nullable = false)
    private Integer driverNumber;

    @Column(name = "session_key", nullable = false)
    private Integer sessionKey;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "name_acronym")
    private String nameAcronym;

    @Column(name = "headshot_url")
    private String headshotUrl;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "team_colour")
    private String teamColour;
}
