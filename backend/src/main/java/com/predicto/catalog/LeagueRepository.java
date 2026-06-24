package com.predicto.catalog;

import com.predicto.common.enums.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeagueRepository extends JpaRepository<League, UUID> {

    List<League> findByGame(Game game);

    Optional<League> findByExternalId(String externalId);

    List<League> findAllByExternalId(String externalId);
}
