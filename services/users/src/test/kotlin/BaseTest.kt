import org.example.UsersApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    classes = [
        UsersApplication::class
    ]
)
@ActiveProfiles("test")
open class BaseTest