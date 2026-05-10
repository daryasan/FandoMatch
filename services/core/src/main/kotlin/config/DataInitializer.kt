package org.example.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.models.db_models.FandomCategory
import org.example.repository.FandomCategoryRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DataInitializer(
    private val fandomCategoryRepository: FandomCategoryRepository
) : ApplicationRunner {

    private val logger = KotlinLogging.logger {}

    private val categories = listOf(
        "ANIME_MANGA",
        "BOOKS",
        "CARTOONS",
        "FILMS",
        "TV_SERIES",
        "GAMES",
        "TABLETOP_GAMES",
        "MUSIC",
        "THEATER_MUSICALS",
        "PODCASTS",
        "COMICS",
        "CELEBRITIES",
        "SPORTS",
        "HISTORY",
        "MYTHOLOGY",
        "OTHER"
    )

    override fun run(args: ApplicationArguments) {
        val existing = fandomCategoryRepository.findAll().map { it.name }.toSet()
        val toInsert = categories.filter { it !in existing }
        if (toInsert.isEmpty()) {
            logger.info { "Fandom categories already seeded, skipping" }
            return
        }
        fandomCategoryRepository.saveAll(toInsert.map { FandomCategory(id = UUID.randomUUID(), name = it) })
        logger.info { "Seeded ${toInsert.size} fandom categories: $toInsert" }
    }
}
