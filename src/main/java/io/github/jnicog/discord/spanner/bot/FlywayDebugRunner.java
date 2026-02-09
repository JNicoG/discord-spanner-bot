package io.github.jnicog.discord.spanner.bot;

import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Component
public class FlywayDebugRunner implements CommandLineRunner {

    private final ApplicationContext ctx;

    public FlywayDebugRunner(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run(String... args) {
        System.out.println("========================================");
        System.out.println("DEBUG: Diagnostic Check");

        // 1. Check if Flyway Bean exists
        if (ctx.containsBean("flyway")) {
            System.out.println("DEBUG: Flyway bean EXISTS.");
            Flyway flyway = ctx.getBean(Flyway.class);
            System.out.println("DEBUG: Flyway Source: " + flyway.getConfiguration().getDataSource());
            System.out.println("DEBUG: Flyway Locations: " + Arrays.toString(flyway.getConfiguration().getLocations()));
            System.out.println("DEBUG: Flyway Baseline On Migrate: " + flyway.getConfiguration().isBaselineOnMigrate());
        } else {
            System.out.println("DEBUG: Flyway bean is MISSING! Auto-configuration did not run.");
        }

        // 2. Check Data Source
        if (ctx.containsBean("dataSource")) {
            System.out.println("DEBUG: DataSource bean EXISTS.");
        } else {
            System.out.println("DEBUG: DataSource bean is MISSING.");
        }

        System.out.println("========================================");
    }
}