package space.yaroslav.familybot.controllers

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import space.yaroslav.familybot.repos.QuoteRepository

@Controller
class QuoteController(private val quoteRepository: QuoteRepository) {

    @PostMapping("/quote")
    fun accept(@RequestBody quote: QuoteDTO): ResponseEntity<Any> {
        quoteRepository.addQuote(quote)
        return ResponseEntity.ok().build()
    }
}

