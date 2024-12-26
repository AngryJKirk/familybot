package dev.storozhenko.familybot.executors

import dev.storozhenko.familybot.common.extensions.context
import dev.storozhenko.familybot.core.models.telegram.stickers.Sticker
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.feature.talking.executors.KeyWordExecutor
import dev.storozhenko.familybot.infrastructure.botConfig
import dev.storozhenko.familybot.infrastructure.createSimpleContext
import dev.storozhenko.familybot.infrastructure.dictionary
import dev.storozhenko.familybot.infrastructure.singleStickerUpdate
import dev.storozhenko.familybot.suits.ExecutorTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.telegram.telegrambots.meta.api.methods.send.SendSticker

@ExtendWith(SpringExtension::class)
class KeyWordExecutorTest : ExecutorTest() {

    @Autowired
    lateinit var keyWordExecutor: KeyWordExecutor

    companion object {
        private val leftHelloSticker = singleStickerUpdate(Sticker.LEFT_HELLO)
        private val rightHelloSticker = singleStickerUpdate(Sticker.RIGHT_HELLO)
        private val noHelloSticker = singleStickerUpdate(Sticker.SWEET_DREAMS)
    }

    override fun priorityTest() {
        val context = createSimpleContext()
        Assertions.assertEquals(Priority.VERY_LOW, keyWordExecutor.priority(context))
    }

    override fun canExecuteTest() {
        Assertions.assertTrue(keyWordExecutor.canExecute(leftHelloSticker.context(botConfig, dictionary, client)))
        Assertions.assertTrue(keyWordExecutor.canExecute(rightHelloSticker.context(botConfig, dictionary, client)))
        Assertions.assertFalse(keyWordExecutor.canExecute(noHelloSticker.context(botConfig, dictionary, client)))
    }

    override fun executeTest() {
        runBlocking { keyWordExecutor.execute(leftHelloSticker.context(botConfig, dictionary, client)) }
        runBlocking { keyWordExecutor.execute(rightHelloSticker.context(botConfig, dictionary, client)) }
        verify(client, times(2)).execute(any<SendSticker>())
    }
}
