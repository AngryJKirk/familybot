package space.yaroslav.familybot.route.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.ConfigType
import space.yaroslav.familybot.common.KeywordConfig
import space.yaroslav.familybot.common.random
import space.yaroslav.familybot.common.toUser
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository
import space.yaroslav.familybot.repos.ifaces.ConfigRepository
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.Priority
import java.util.concurrent.ThreadLocalRandom

@Component
class KeyWordExecutor(val keyset: ChatLogRepository,
                      val configRepository: ConfigRepository) : Executor {
    override fun priority(): Priority {
        return if(getConfig().rageMode){
            Priority.HIGH
        } else{
            Priority.LOW
        }
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val config: KeywordConfig = configRepository.get(ConfigType.KEYWORD) as KeywordConfig
        if (config.randomPower == 0 || ThreadLocalRandom.current().nextInt(0, config.randomPower) == 0) {
            val get = keyset.get(update.message.from.toUser(telegramChat = update.message.chat))
            if (get.size < 100) return {}
            return {
                val message = if (config.rageMode){
                    rageModeFormat(get.random()!!)
                } else {
                    get.random()
                }

                it.execute(SendMessage(update.message.chatId, message)
                        .setReplyToMessageId(update.message.messageId))
            }
        } else {
            return {}
        }

    }

    override fun canExecute(message: Message): Boolean {
        return getConfig().rageMode
    }

    private fun rageModeFormat(string: String): String{
        return string.toUpperCase() + "!!!!"
    }

    private fun getConfig(): KeywordConfig{
        return configRepository.get(ConfigType.KEYWORD) as KeywordConfig
    }
}