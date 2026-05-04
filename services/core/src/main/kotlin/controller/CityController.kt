package org.example.controller

import com.fandomatch.core.model.CitySearchResponse
import com.fandomatch.core.model.CitySearchData
import com.fandomatch.core.model.ResponseStatus
import io.github.oshai.kotlinlogging.KLogging
import org.example.util.CITY_MAP
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/core/cities")
class CityController {

    companion object : KLogging()

    @GetMapping("/search")
    fun searchCities(
        @RequestParam query: String
    ): ResponseEntity<CitySearchResponse> {
        logger.info("GET /core/cities/search called with query=$query")
        val lowerQuery = query.lowercase()
        val cities = CITY_MAP.values
            .filter { city ->
                city.nameEn.lowercase().contains(lowerQuery) ||
                        city.nameRu.lowercase().contains(lowerQuery)
            }
        return ResponseEntity.ok(
            CitySearchResponse(
                status = ResponseStatus.SUCCESS,
                successResponse = CitySearchData(cities = cities)
            )
        )
    }
}
