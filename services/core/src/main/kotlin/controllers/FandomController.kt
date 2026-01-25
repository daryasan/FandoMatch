package controllers

import com.fandomatch.core.api.FandomApi
import com.fandomatch.core.model.FandomListResponse
import org.example.service.FandomService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class FandomController(
    private val fandomService: FandomService
) : FandomApi {

    override fun coreFandomsGetPost(): ResponseEntity<FandomListResponse> {
        return ResponseEntity.ok(fandomService.getAllFandoms())
    }
}

