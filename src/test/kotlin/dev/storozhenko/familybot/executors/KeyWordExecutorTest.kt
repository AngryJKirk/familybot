package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import space.yaroslav.familybot.executors.eventbased.keyword.KeyWordExecutor
import space.yaroslav.familybot.infrastructure.createSimpleContext
import space.yaroslav.familybot.infrastructure.singleStickerContext
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.models.telegram.stickers.Sticker
import space.yaroslav.familybot.suits.ExecutorTest

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
