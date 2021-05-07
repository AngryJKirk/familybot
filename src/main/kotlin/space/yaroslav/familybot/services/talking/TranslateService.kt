package space.yaroslav.familybot.services.talking

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.telegram.BotConfig

@Component
class TranslateService(private val botConfig: BotConfig) {
    private val log = getLogger()
    private val restTemplate = RestTemplate()
    private val yandexUrl = "https://translate.api.cloud.yandex.net/translate/v2/translate"

    fun translate(message: String): String {
        if (botConfig.yandexKey.isNullOrBlank()) {
            log.warn("Yandex Translate API Key is not set, falling back to default language")
            return message
        }
        return runCatching {
            callApi(message)
        }.getOrElse {
            log.error("Yandex API failed", it)
            message
        }
    }

    private fun callApi(message: String): String {
        val headers = HttpHeaders().apply {
            this["Authorization"] = "Api-Key ${botConfig.yandexKey}"
        }
        val entity = HttpEntity(
            mapOf(
                "texts" to listOf(message),
                "targetLanguageCode" to "uk"
            ),
            headers
        )
        return restTemplate
            .exchange(yandexUrl, HttpMethod.POST, entity, YandexTranslateResponse::class.java)
            .body
            .translations
            .first()
            .text
    }
}

data class YandexTranslateResponse(
    @JsonProperty("translations") val translations: List<YandexTranslation>
)

data class YandexTranslation(
    @JsonProperty("text") val text: String,
    @JsonProperty("detectedLanguageCode") val detectedLanguageCode: String
)
