package dev.storozhenko.familybot.feature.help


import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import org.springframework.stereotype.Component

@Component
class PrivacyCommandExecutor : CommandExecutor() {
    override fun command() = Command.PRIVACY

    override suspend fun execute(context: ExecutorContext) {
        context.send( """
        Privacy Policy
        
        1. Data Collection
        
        Our Telegram bot collects and stores certain messages and usernames.
        
        2. Data Usage
        
        The collected data is used solely for the functionality of the bot.
        We do not share this data with third parties or use it for any other purpose.
        
        3. Data Deletion
        
        Users have the right to request the deletion of all their data.
        To request data deletion, please send a message to the developer via /help command.
        
        4. Security
        
        We implement appropriate measures to protect the collected data from unauthorized access.
        
        5. Changes to This Policy
        
        We may update this privacy policy as necessary.
        Users will be notified of any significant changes.
        
        6. Contact Us
        
        For any questions or concerns about this privacy policy, please contact us through the bot's /help command.
        """.trimIndent()
        )
    }
}