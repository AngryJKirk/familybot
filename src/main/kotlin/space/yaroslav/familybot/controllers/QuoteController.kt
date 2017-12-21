package space.yaroslav.familybot.controllers

import org.springframework.dao.DuplicateKeyException
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import space.yaroslav.familybot.repos.ifaces.QuoteRepository

@Controller
class QuoteController(private val quoteRepository: QuoteRepository) {

    @PostMapping("/quote")
    fun accept(@RequestBody quote: QuoteDTO): ResponseEntity<String> {
        val validate = validate(quote)
        if (validate != null) {
            return ResponseEntity.badRequest().body(validate)
        }
        return try {
            quoteRepository.addQuote(quote)
            ResponseEntity.ok("Ебать красавчик")
        } catch (e: DuplicateKeyException) {
            ResponseEntity.badRequest()
                    .body("Такая цитата уже есть блять")
        }
    }

    private fun validate(quote: QuoteDTO): String? {
        if (quote.tags.all { it.isBlank() }) {
            return "Должен быть хотя бы один тег"
        }
        if (quote.quote.isBlank()) {
            return "Цитату введи, долбоеб"
        }
        return null
    }

    @GetMapping
    fun tagList(): List<String>{
       return quoteRepository.getTags()
    }
}

