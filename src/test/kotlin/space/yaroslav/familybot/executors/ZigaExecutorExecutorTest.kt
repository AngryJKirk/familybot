package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Ignore
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.infrastructure.ActionWithSticker
import space.yaroslav.familybot.infrastructure.UpdateBuilder
import space.yaroslav.familybot.route.executors.eventbased.ZigaExecutor
import space.yaroslav.familybot.route.models.Priority
import space.yaroslav.familybot.route.models.stickers.Sticker
import space.yaroslav.familybot.suits.ExecutorTest
@Ignore("Need some time to adapt test infrastructure to stickers")
class ZigaExecutorExecutorTest : ExecutorTest() {

    @Autowired
    lateinit var zigaExecutor: ZigaExecutor

    companion object {
        private val leftZigaSticker = UpdateBuilder().singleStickerUpdate(Sticker.LEFT_ZIGA)
        private val rightZigaSticker = UpdateBuilder().singleStickerUpdate(Sticker.RIGHT_ZIGA)
        private val noZigaSticker = UpdateBuilder().singleStickerUpdate(Sticker.SWEET_DREAMS)
    }

    override fun priotityTest() {
        val update = UpdateBuilder().build()
        Assert.assertEquals(Priority.LOW, zigaExecutor.priority(update))
    }

    override fun canExecuteTest() {
        Assert.assertTrue(zigaExecutor.canExecute(leftZigaSticker.message))
        Assert.assertTrue(zigaExecutor.canExecute(rightZigaSticker.message))
        Assert.assertFalse(zigaExecutor.canExecute(noZigaSticker.message))
    }

    override fun executeTest() {
        runBlocking { zigaExecutor.execute(leftZigaSticker).invoke(testSender) }
        Assert.assertTrue(testSender.actions.first() is ActionWithSticker)
        runBlocking { zigaExecutor.execute(rightZigaSticker).invoke(testSender) }
        Assert.assertTrue(testSender.actions.first() is ActionWithSticker)
    }
}
