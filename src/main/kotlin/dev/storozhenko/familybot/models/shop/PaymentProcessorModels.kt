package dev.storozhenko.familybot.models.shop

import org.telegram.telegrambots.meta.bots.AbsSender
import dev.storozhenko.familybot.models.dictionary.Phrase

sealed class PreCheckOutResponse(val success: Boolean) {
    class Success : PreCheckOutResponse(success = true)

    class Error(val explainPhrase: Phrase) : PreCheckOutResponse(success = false)
}

data class SuccessPaymentResponse(val phrase: Phrase, val customCall: (AbsSender) -> Unit = {})
