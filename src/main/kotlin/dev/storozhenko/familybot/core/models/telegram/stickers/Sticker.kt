package dev.storozhenko.familybot.core.models.telegram.stickers

enum class Sticker(val pack: StickerPack, val stickerEmoji: String) {
    LEFT_HELLO(StickerPack.FAMILY_PACK, "\uD83D\uDD90"),
    RIGHT_HELLO(StickerPack.FAMILY_PACK, "\uD83E\uDD1A"),
    SWEET_DREAMS(StickerPack.FAMILY_PACK, "\uD83C\uDF19"),
}
