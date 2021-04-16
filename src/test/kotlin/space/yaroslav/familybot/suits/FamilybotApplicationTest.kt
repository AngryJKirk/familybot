package space.yaroslav.familybot.suits

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.testcontainers.containers.GenericContainer
import org.testcontainers.images.builder.ImageFromDockerfile
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

@SpringBootTest
@ActiveProfiles("testing")
@ExtendWith(SpringExtension::class)
abstract class FamilybotApplicationTest {

    class KGenericContainer(imageName: Future<String>) : GenericContainer<KGenericContainer>(imageName)

    @MockBean
    lateinit var botMock: TelegramBotsApi

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
            System.setProperty("spring.redis.port", redisContainer.firstMappedPort.toString())
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
