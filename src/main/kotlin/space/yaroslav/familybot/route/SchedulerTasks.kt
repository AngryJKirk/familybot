package space.yaroslav.familybot.route

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import space.yaroslav.familybot.repos.ifaces.CommonRepository

@Component
class SchedulerTasks(val bot: TelegramLongPollingBot, val commonRepository: CommonRepository) {

    @Scheduled(cron = "0 20 4 * * *", zone = "Europe/Moscow")
    fun water() {
        commonRepository.getChats().forEach { bot.execute(SendMessage(it.id, "БЛЯЯЯ ВОДЫЫЫЫЫЫЫЫЫЫЫЫ")) }
    }
}
