package space.yaroslav.familybot.executors

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.telegram.telegrambots.meta.api.objects.Message
import space.yaroslav.familybot.infrastructure.ActionWithText
import space.yaroslav.familybot.infrastructure.UpdateBuilder
import space.yaroslav.familybot.route.executors.eventbased.HuificatorExecutor
import space.yaroslav.familybot.route.models.Priority
import space.yaroslav.familybot.suits.ExecutorTest

class HuificatorExecutorTest : ExecutorTest() {

    val huificatorExecutor: HuificatorExecutor = HuificatorExecutor(0)

    override fun priotityTest() {
        val update = UpdateBuilder().build()
        val priority = huificatorExecutor.priority(update)
        Assert.assertEquals("Huificator executor should be random", Priority.RANDOM, priority)
    }

    override fun canExecuteTest() {
        val canExecute = huificatorExecutor.canExecute(Message())
        Assert.assertFalse("Should always be not available to execute", canExecute)
    }

    override fun executeTest() {
        val testSet = mapOf(
            "штош" to null,
            "как-нибудь" to "хуяк-нибудь",
            "дерево" to "хуерево",
            "hello" to null,
            "куку шуку пидор" to "хуидор",
            "куку шуку             петух" to "хуетух",
            "удача" to "хуюдача",
            "х-у-й-ня" to null
        )
        testSet
            .mapKeys { word -> UpdateBuilder().simpleTextMessageFromUser(word.key) }
            .forEach { huificateEntry ->
                runBlocking { huificatorExecutor.execute(huificateEntry.key).invoke(testSender) }
                val actions = testSender.actions
                if (huificateEntry.value == null) {
                    takeIf { actions.isEmpty() } ?: throw AssertionError("Should not have text action $actions in case of $huificateEntry")
                } else {
                    val action = actions
                        .firstOrNull() as? ActionWithText ?: throw AssertionError("Should have text action in case of $huificateEntry")
                    Assert.assertEquals(huificateEntry.value, action.content)
                }
                cleanSender()
            }
    }
}
