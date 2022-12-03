package dev.storozhenko.familybot.executors

import dev.storozhenko.familybot.executors.eventbased.keyword.KeyWordExecutor
import dev.storozhenko.familybot.infrastructure.createSimpleContext
import dev.storozhenko.familybot.infrastructure.singleStickerContext
import dev.storozhenko.familybot.models.router.Priority
import dev.storozhenko.familybot.models.telegram.stickers.Sticker
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
        private val leftZigaSticker = singleStickerContext(Sticker.LEFT_ZIGA)
        private val rightZigaSticker = singleStickerContext(Sticker.RIGHT_ZIGA)
        private val noZigaSticker = singleStickerContext(Sticker.SWEET_DREAMS)
    }

    override fun priorityTest() {
        val context = createSimpleContext()
        Assertions.assertEquals(Priority.VERY_LOW, keyWordExecutor.priority(context))
    }

    override fun canExecuteTest() {
        Assertions.assertTrue(keyWordExecutor.canExecute(leftZigaSticker))
        Assertions.assertTrue(keyWordExecutor.canExecute(rightZigaSticker))
        Assertions.assertFalse(keyWordExecutor.canExecute(noZigaSticker))
    }

    override fun executeTest() {
        runBlocking { keyWordExecutor.execute(leftZigaSticker).invoke(sender) }
        runBlocking { keyWordExecutor.execute(rightZigaSticker).invoke(sender) }
        verify(sender, times(2)).execute(any<SendSticker>())
    }
}
