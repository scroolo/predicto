package com.predicto.wallet;

import com.predicto.auth.User;
import com.predicto.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    private Integer balance = 100;

    @Column(name = "lifetime_wagered_lol")
    @Builder.Default
    private Integer lifetimeWageredLol = 0;

    @Column(name = "lifetime_wagered_cs2")
    @Builder.Default
    private Integer lifetimeWageredCs2 = 0;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
