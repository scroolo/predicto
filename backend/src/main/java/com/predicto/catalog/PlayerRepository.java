package com.predicto.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {

    List<Player> findByTeamId(UUID teamId);

    Optional<Player> findByExternalId(String externalId);

    List<Player> findAllByExternalId(String externalId);
}
