package dev.storozhenko.familybot.suits

import dev.storozhenko.familybot.core.telegram.BotStarter
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.GenericContainer
import org.testcontainers.images.builder.ImageFromDockerfile
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

@SpringBootTest
@ActiveProfiles(BotStarter.TESTING_PROFILE_NAME)
@ExtendWith(SpringExtension::class)
abstract class FamilybotApplicationTest {

    class KGenericContainer(imageName: Future<String>) : GenericContainer<KGenericContainer>(imageName)

    companion object {
        init {
            createPostgres()
            createRedis()
        }

        private fun createRedis() {
            val redisContainer: KGenericContainer = KGenericContainer(
                CompletableFuture.supplyAsync { "redis:latest" }
            ).withExposedPorts(6379)
            redisContainer.start()
            System.setProperty("spring.data.redis.port", redisContainer.firstMappedPort.toString())
        }

        private fun createPostgres() {
            val postgresContainer: KGenericContainer = KGenericContainer(
                ImageFromDockerfile()
                    .withFileFromPath("Dockerfile", Paths.get("src/test/resources/Dockerfile"))
                    .withFileFromPath("db.sql", Paths.get("scripts/db.sql"))
                    .withFileFromPath("db_test_assets.sql", Paths.get("src/test/resources/db_test_assets.sql"))
            ).withExposedPorts(5432)
            postgresContainer.start()
            System.setProperty(
                "spring.datasource.url",
                "jdbc:postgresql://localhost:${postgresContainer.firstMappedPort}/postgres"
            )
        }
    }
}
