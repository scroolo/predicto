package com.predicto.rank;

import com.predicto.common.enums.Game;
import com.predicto.season.RankTier;
import com.predicto.season.RankTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankService {

    private final RankTierRepository rankTierRepository;

    public RankTier resolveRank(Game game, Integer elo) {
        int e = elo != null ? elo : 0;
        return rankTierRepository.findByGameOrderBySortOrderAsc(game)
                .stream()
                .filter(t -> t.getMinWagered() <= e)
                .reduce((first, second) -> second)
                .orElse(null);
    }
}
