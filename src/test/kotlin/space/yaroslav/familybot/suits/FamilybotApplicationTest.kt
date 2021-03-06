package space.yaroslav.familybot.suits

import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.testcontainers.containers.GenericContainer
import org.testcontainers.images.builder.ImageFromDockerfile
import java.nio.file.Paths
import java.util.concurrent.Future

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("testing")
abstract class FamilybotApplicationTest {

    class KGenericContainer(imageName: Future<String>) : GenericContainer<KGenericContainer>(imageName)

    @MockBean
    lateinit var botMock: TelegramBotsApi

    companion object {
        init {
            val postgresContainer: KGenericContainer = KGenericContainer(
                ImageFromDockerfile()
                    .withFileFromPath("Dockerfile", Paths.get("src/main/resources/database/Dockerfile"))
                    .withFileFromPath("db.sql", Paths.get("src/main/resources/database/db.sql"))
                    .withFileFromPath("db_test_assets.sql", Paths.get("src/test/resources/db_test_assets.sql"))
            ).withExposedPorts(5432)
            postgresContainer.start()
            System.setProperty(
                "spring.datasource.url",
                "jdbc:postgresql://localhost:${postgresContainer.getMappedPort(5432)}/postgres"
            )
        }
    }
}
