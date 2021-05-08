package space.yaroslav.familybot.telegram

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("payments")
class PaymentConfig {
    var token: String? = null
}
