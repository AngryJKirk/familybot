package space.yaroslav.familybot.route.services

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.common.base.Suppliers
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.io.InputStream
import java.net.URI
import java.util.concurrent.TimeUnit

@Component
class TextToSpeechService(
    @Value("\${yandex.token}") private val token: String,
    @Value("\${yandex.folder-id}") private val folderId: String
) {

    private val auth = Suppliers.memoizeWithExpiration(::getAuthToken, 10, TimeUnit.HOURS)
    private val restTemplate = RestTemplate()
    private val authUrl = URI("https://iam.api.cloud.yandex.net/iam/v1/tokens")
    private val speechUrl = URI("https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize")
    private fun getAuthToken(): String {
        return restTemplate.postForEntity(authUrl, YandexAuthRequest(token), YandexAuthResponse::class.java)
            .body.iamToken
    }

    fun toSpeech(speech: String, emotion: YandexSpeechType = YandexSpeechType.NEUTRAL): InputStream {
        val speed = if (speech.split(" ").size < 10) {
            "0.8"
        } else {
            "1.0"
        }
        val bearer = auth.get()
        val entity = HttpEntity(LinkedMultiValueMap<String, String>().apply {
            put("text", listOf(speech))
            put("emotion", listOf(emotion.name.toLowerCase()))
            put("voice", listOf("ermil"))
            put("speed", listOf(speed))
            put("folderId", listOf(folderId))
        }, HttpHeaders().apply {
            put("Authorization", listOf("Bearer $bearer"))
            put("Transfer-Encoding", listOf("chunked"))
        })
        return restTemplate.postForEntity(speechUrl, entity, Resource::class.java).body.inputStream
    }
}

class YandexAuthRequest(val yandexPassportOauthToken: String)
class YandexAuthResponse(@JsonProperty("iamToken") val iamToken: String)
enum class YandexSpeechType {
    EVIL,
    GOOD,
    NEUTRAL
}
