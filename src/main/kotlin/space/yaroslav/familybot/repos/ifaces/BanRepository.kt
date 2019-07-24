package space.yaroslav.familybot.repos.ifaces

import space.yaroslav.familybot.route.services.ban.Ban

interface BanRepository {
    fun addBan(banEntity: BanEntity, ban: Ban)
    fun reduceBan(ban: Ban)
    fun getByEntity(banEntity: BanEntity): Ban?
}

enum class BanEntityType(val entityTypeId: Int) {
    USER(1),
    CHAT(2)
}

data class BanEntity(val entityId: Long, val entityType: BanEntityType)
