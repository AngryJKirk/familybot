package space.yaroslav.familybot.models.telegram

data class Chat(val id: Long, val name: String?, val idString: String = id.toString())