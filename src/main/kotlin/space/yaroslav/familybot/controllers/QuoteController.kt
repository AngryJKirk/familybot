package space.yaroslav.familybot.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import space.yaroslav.familybot.repos.QuoteRepository

@Controller
class QuoteController(private val quoteRepository: QuoteRepository) {

    @PostMapping("/quote")
    fun accept(@ModelAttribute quote: QuoteDTO): String {
        quoteRepository.addQuote(quote)
        return "/"

    }

    @GetMapping("/")
    fun getPage(model: Model): String {
        model.addAttribute("quote", QuoteDTO())
        return "quote"
    }
}

