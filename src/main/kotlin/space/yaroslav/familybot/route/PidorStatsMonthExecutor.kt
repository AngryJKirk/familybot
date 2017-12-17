package space.yaroslav.familybot.route

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender

@Component
class PidorStatsMonthExecutor : CommandExecutor() {
    override fun command(): Command {
        return Command.STATS_MONTH
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}