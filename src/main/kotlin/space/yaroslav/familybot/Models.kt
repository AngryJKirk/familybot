package space.yaroslav.familybot

import java.time.LocalDateTime


data class User(val id: Long, val chat: Chat, val name: String, val nickname: String)

data class Chat(val id: Long, val name: String)

data class Pidor(val id: Long, val user: User, val date: LocalDateTime)


