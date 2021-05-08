package space.yaroslav.familybot.models.telegram

import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.telegram.User
import java.time.Instant

data class CommandByUser(val user: User, val command: Command, val date: Instant)