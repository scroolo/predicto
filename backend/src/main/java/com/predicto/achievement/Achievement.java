package com.predicto.achievement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "achievements")
@Getter @Setter
public class Achievement {
    @Id
    private String id;
    private String name;
    private String description;
    private String category;
    private String rarity;
    private String icon;
    private int points;
    private boolean hidden;
}
