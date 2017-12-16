package space.yaroslav.familybot.controllers;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorControllerImpl implements ErrorController {

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public String error() {
        return "<h1><center>ДУРАК, ТАКАЯ ЦЫТАТА УЖЕ ЕСТЬ!!!<center><h1>" +
                "<form action=\"/\" method=\"get\">\n" +
                "    <input type=\"submit\" value=\"ПОПРОБОВАТЬ НЕ ОБОСРАТЬСЯ ЕЩЕ РАЗ\" \n" +
                "         name=\"Submit\" id=\"frm1_submit\" />\n" +
                "</form>" ;
    }


    public String getErrorPath() {
        return PATH;
    }
}