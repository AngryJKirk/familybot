package space.yaroslav.familybot.controllers

import org.springframework.boot.autoconfigure.web.ErrorController
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ErrorControllerImpl : ErrorController {

    @RequestMapping(value = PATH)
    fun error(): ResponseEntity<Any> {
        return ResponseEntity.badRequest().build();
    }


    override fun getErrorPath(): String {
        return PATH
    }

    companion object {

        private const val PATH = "/error"
    }
}