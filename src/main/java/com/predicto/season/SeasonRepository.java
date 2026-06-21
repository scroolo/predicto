package com.predicto.season;

import com.predicto.common.enums.Game;
import com.predicto.common.enums.SeasonStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeasonRepository extends JpaRepository<Season, UUID> {

    List<Season> findByGame(Game game);

    List<Season> findByStatus(SeasonStatus status);

    List<Season> findByGameAndStatus(Game game, SeasonStatus status);
}
