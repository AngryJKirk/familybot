package space.yaroslav.familybot.controllers

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import space.yaroslav.familybot.repos.ifaces.QuoteRepository

@RestController
class QuoteController(private val quoteRepository: QuoteRepository) {

    @PostMapping("/quote")
    fun accept(@RequestBody quote: QuoteDTO): ResponseEntity<Response> {
        val validate = validate(quote)
        if (validate != null) {
            return ResponseEntity.badRequest().body(validate)
        }
        return try {
            quoteRepository.addQuote(quote)
            ResponseEntity.ok(Response("Ебать красавчик"))
        } catch (e: DuplicateKeyException) {
            ResponseEntity.badRequest()
                    .body(Response("Такая цитата уже есть блять"))
        }
    }

    private fun validate(quote: QuoteDTO): Response? {
        if (quote.tags.all { it.isBlank() }) {
            return Response("Должен быть хотя бы один тег")
        }
        if (quote.quote.isBlank()) {
            return Response("Цитату введи, долбоеб")
        }
        return null
    }

    @GetMapping
    fun tagList(): List<String>{
       return quoteRepository.getTags()
    }
}


data class Response(@JsonProperty("message") val message: String)