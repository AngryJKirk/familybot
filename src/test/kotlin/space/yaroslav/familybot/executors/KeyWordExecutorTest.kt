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
import space.yaroslav.familybot.infrastructure.createSimpleUpdate
import space.yaroslav.familybot.infrastructure.singleStickerUpdate
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.models.stickers.Sticker
import space.yaroslav.familybot.suits.ExecutorTest

@ExtendWith(SpringExtension::class)
class KeyWordExecutorTest : ExecutorTest() {

    @Autowired
    lateinit var keyWordExecutor: KeyWordExecutor

    companion object {
        private val leftZigaSticker = singleStickerUpdate(Sticker.LEFT_ZIGA)
        private val rightZigaSticker = singleStickerUpdate(Sticker.RIGHT_ZIGA)
        private val noZigaSticker = singleStickerUpdate(Sticker.SWEET_DREAMS)
    }

    override fun priorityTest() {
        val update = createSimpleUpdate()
        Assertions.assertEquals(Priority.LOW, keyWordExecutor.priority(update))
    }

    override fun canExecuteTest() {
        Assertions.assertTrue(keyWordExecutor.canExecute(leftZigaSticker.message))
        Assertions.assertTrue(keyWordExecutor.canExecute(rightZigaSticker.message))
        Assertions.assertFalse(keyWordExecutor.canExecute(noZigaSticker.message))
    }

    override fun executeTest() {
        runBlocking { keyWordExecutor.execute(leftZigaSticker).invoke(sender) }
        runBlocking { keyWordExecutor.execute(rightZigaSticker).invoke(sender) }
        verify(sender, times(2)).execute(any<SendSticker>())
    }
}
