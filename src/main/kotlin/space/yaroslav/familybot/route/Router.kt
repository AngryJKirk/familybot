package space.yaroslav.familybot.route

import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Chat
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.CommandByUser
import space.yaroslav.familybot.common.utils.isGroup
import space.yaroslav.familybot.common.utils.random
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.FunctionsConfigureRepository
import space.yaroslav.familybot.repos.ifaces.HistoryRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.executors.command.CommandExecutor
import space.yaroslav.familybot.route.executors.eventbased.AntiDdosExecutor
import space.yaroslav.familybot.route.models.Priority
import java.time.Instant


@Component
class Router(val repository: CommonRepository,
             val historyRepository: HistoryRepository,
             val executors: List<Executor>,
             val chatLogRepository: ChatLogRepository,
             val configureRepository: FunctionsConfigureRepository) {

    private final val logger = LoggerFactory.getLogger(Router::class.java)

    fun processUpdate(update: Update): (AbsSender) -> Unit {

        val message = update.message ?: update.callbackQuery?.message
        ?: return { logger.info("Empty message was given: $update") }

        val chat = message.chat

        if (!chat.isGroup()) {
            return { logger.warn("Someone try to do from outside of groups: $update") }
        }

        launch { register(message) }

        val executor = selectExecutor(update) ?: selectLowPriority(update)

        logger.info("Executor to apply: ${executor.javaClass.simpleName}")

        return if (isExecutorDisabled(executor, chat)) {
            when (executor) {
                is CommandExecutor -> disabledCommand(chat)
                is AntiDdosExecutor -> antiDdosSkip(message, update)
                else -> { _ -> logger.info("Skip event executor due to configuration") }
            }
        } else {
            executor.execute(update)
        }.also { launch { logChatCommand(executor, update) } }
    }

    private fun selectLowPriority(update: Update): Executor {
        logger.info("No executor found, trying to find low priority executors")

        launch { logChatMessage(update) }

        val executor = executors
                .filter { it.priority(update) == Priority.LOW }
                .random()!!

        logger.info("Low priority executor ${executor.javaClass.simpleName} was selected")
        return executor
    }


    private fun antiDdosSkip(message: Message, update: Update): (AbsSender) -> Unit = { it ->
        logger.info("Skip anti-ddos executor due to configuration")
        val executor = executors
                .filter { it is CommandExecutor }
                .find { it.canExecute(message) }
        val function = if (isExecutorDisabled(executor, message.chat)) {
            disabledCommand(message.chat)
        } else {
            executor?.execute(update)
        }

        function?.invoke(it)
    }


    private fun disabledCommand(chat: Chat): (AbsSender) -> Unit = { it ->
        logger.info("Skip command executor due to configuration")
        it.execute(SendMessage(chat.id, "Команда выключена, сорян"))
    }


    private fun isExecutorDisabled(executor: Executor?, chat: Chat): Boolean {
        return executor is Configurable && !configureRepository.isEnabled(executor.getFunctionId(), chat.toChat())
    }

    private fun logChatCommand(executor: Executor?, update: Update) {
        if (executor is CommandExecutor && executor.isLoggable()) {
            historyRepository.add(CommandByUser(
                    update.toUser(),
                    executor.command(),
                    Instant.now()))
        }
    }


    private fun logChatMessage(update: Update) {
        val text = update.message?.text
        if (text != null && text.split(" ").size >= 3) {
            chatLogRepository.add(update.toUser(), text)
        }
    }

    private fun selectExecutor(update: Update): Executor? {
        return executors
                .sortedByDescending { it.priority(update).int }
                .filter { it.priority(update).int >= 0 }
                .find { it.canExecute(update.message ?: update.callbackQuery.message) }
    }


    private fun register(message: Message) {
        val chat = message.chat.toChat()

        if (!repository.containsChat(chat)) {
            repository.addChat(chat)
        }

        if (message.leftChatMember != null) {
            repository.changeUserActiveStatus(message.leftChatMember.toUser(chat = chat), false)
        } else if (message.newChatMembers?.isNotEmpty() == true) {
            message.newChatMembers.filter { !it.bot }.forEach { repository.addUser(it.toUser(chat = chat)) }
        } else if (!message.from.bot) {
            repository.addUser(message.from.toUser(chat = chat))
        }
    }
}