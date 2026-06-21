package com.predicto.catalog;

import com.predicto.common.enums.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    List<Team> findByLeagueId(UUID leagueId);

    List<Team> findByGame(Game game);

    Optional<Team> findByExternalId(String externalId);
}
