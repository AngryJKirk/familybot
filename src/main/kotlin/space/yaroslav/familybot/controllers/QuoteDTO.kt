package space.yaroslav.familybot.controllers

import com.fasterxml.jackson.annotation.JsonProperty

class QuoteDTO(@JsonProperty("quote") val quote: String, @JsonProperty("tags")val tags: List<String>)



