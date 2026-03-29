import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class TestDatabaseConfig : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(context: ConfigurableApplicationContext) {
        val isCi = System.getenv("CI") == "true"

        if (!isCi) {
            TestPostgresContainer.start()

            TestPropertyValues.of(
                "spring.datasource.url=${TestPostgresContainer.jdbcUrl}",
                "spring.datasource.username=${TestPostgresContainer.username}",
                "spring.datasource.password=${TestPostgresContainer.password}",
            ).applyTo(context.environment)
        }
    }
}

object TestPostgresContainer :
    PostgreSQLContainer<TestPostgresContainer>(
        DockerImageName.parse("postgres:15")
    ) {

    init {
        withDatabaseName("fdmatch_core_test")
        withUsername("testuser")
        withPassword("testpassword")
        withReuse(true)
    }
}
