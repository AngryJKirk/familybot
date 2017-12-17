package space.yaroslav.familybot.controllers

import org.springframework.boot.autoconfigure.web.ErrorController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ErrorControllerImpl : ErrorController {

    @RequestMapping(value = PATH)
    fun error(): String {
        return "<h1><center>ТЫ НАТВОРИЛ КАКУЮ-ТО ХУЙНЮ<center><h1>" +
                "<form action=\"quote\" method=\"get\">\n" +
                "    <input type=\"submit\" value=\"ПОПРОБОВАТЬ НЕ ОБОСРАТЬСЯ ЕЩЕ РАЗ\" \n" +
                "         name=\"Submit\" id=\"frm1_submit\" />\n" +
                "</form>"
    }


    override fun getErrorPath(): String {
        return PATH
    }

    companion object {

        private const val PATH = "/error"
    }
}