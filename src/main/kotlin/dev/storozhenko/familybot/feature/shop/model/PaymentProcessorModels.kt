package dev.storozhenko.familybot.feature.shop.model

import dev.storozhenko.familybot.core.models.dictionary.Phrase
import org.telegram.telegrambots.meta.bots.AbsSender

sealed class PreCheckOutResponse(val success: Boolean) {
    class Success : PreCheckOutResponse(success = true)

    class Error(val explainPhrase: Phrase) : PreCheckOutResponse(success = false)
}

data class SuccessPaymentResponse(val phrase: Phrase, val customCall: (AbsSender) -> Unit = {})
