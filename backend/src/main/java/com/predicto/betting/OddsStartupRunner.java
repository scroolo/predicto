package com.predicto.betting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class OddsStartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OddsStartupRunner.class);

    private final OddsCalculationService oddsCalculationService;

    public OddsStartupRunner(OddsCalculationService oddsCalculationService) {
        this.oddsCalculationService = oddsCalculationService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Running startup odds calculation...");
        try {
            int count = oddsCalculationService.calculateOddsForAllUpcomingMatches();
            log.info("Startup odds calculation complete: {} matches updated", count);
        } catch (Exception e) {
            log.error("Startup odds calculation failed", e);
        }
    }
}
