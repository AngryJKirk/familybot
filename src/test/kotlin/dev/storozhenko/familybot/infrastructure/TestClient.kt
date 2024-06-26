package dev.storozhenko.familybot.infrastructure

import dev.storozhenko.familybot.core.models.telegram.stickers.Sticker
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.methods.stickers.GetStickerSet
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberMember
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.stickers.StickerSet
import org.telegram.telegrambots.meta.generics.TelegramClient
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker as TelegramSticker

class TestClient {
    val client = mock<TelegramClient> {
        on { execute(any<SendMessage>()) } doReturn Message()
        on { execute(any<SendSticker>()) } doReturn Message()
        on { execute(any<GetStickerSet>()) } doReturn createStickers()
        on { execute(any<GetChatMember>()) } doAnswer requestedChatMember()
        on { execute(any<GetChatAdministrators>()) } doReturn requestedAdmins()
    }

    private fun requestedChatMember(): (InvocationOnMock) -> ChatMemberMember? =
        {
            val arg = it.arguments.first() as GetChatMember
            val userId = arg.userId
            ChatMemberMember().apply {
                user = User(userId,  "Test user", false)
                user.userName = "user$userId"
                user.lastName = "#$userId"
            }
        }

    private fun requestedAdmins(): ArrayList<ChatMember> {
        return ArrayList(
            (1L..3L).map { userId ->
                ChatMemberAdministrator().apply {
                    user = User(userId, "Test user", false)
                    user.id = userId
                    user.userName = "user$userId"
                    user.lastName = "#$userId"
                }
            },
        )
    }

    private fun createStickers(): StickerSet {
        val stickers = Sticker.entries
            .map { sticker ->
                TelegramSticker().apply {
                    emoji = sticker.stickerEmoji
                    fileId = randomString()
                    setName = sticker.pack.packName
                }
            }
        return StickerSet().apply {
            this.stickers = stickers
        }
    }
}
