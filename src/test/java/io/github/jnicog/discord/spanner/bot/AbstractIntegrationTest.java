package io.github.jnicog.discord.spanner.bot;

import jakarta.transaction.Transactional;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests.
 * <p>
 * This class provides a standard configuration for integration tests, including:
 * <ul>
 *     <li>Spinning up a PostgreSQL container via {@link TestContainersConfig}</li>
 *     <li>Running Flyway migrations to ensure the DB schema is up-to-date</li>
 *     <li>Loading the 'test' profile</li>
 *     <li>Mocking external dependencies like JDA via {@link TestConfig}</li>
 *     <li>Transactional rollback after each test</li>
 * </ul>
 * <p>
 * Tests extending this class will run against a 'production-like' database schema.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import({TestContainersConfig.class, TestConfig.class})
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

}
