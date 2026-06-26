package com.predicto.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
}
