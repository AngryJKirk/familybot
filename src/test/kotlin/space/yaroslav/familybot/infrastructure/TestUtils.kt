package space.yaroslav.familybot.infrastructure

import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload
import java.util.UUID

import java.util.concurrent.ThreadLocalRandom

fun randomString() = UUID.randomUUID().toString()
fun randomInt() = ThreadLocalRandom.current().nextInt(0, 1000000)
fun randomLong() = randomInt().toLong()
fun randomChatId() = listOf(10L, 20L, 30L).random()
fun randomUserId() = listOf(1L, 2L, 3L).random()

fun payload(shopItem: ShopItem) =
    ShopPayload(chatId = randomChatId(), randomUserId(), shopItem)
