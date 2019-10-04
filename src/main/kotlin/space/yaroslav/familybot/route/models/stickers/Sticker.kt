package space.yaroslav.familybot.route.models.stickers

enum class Sticker(val pack: StickerPack, val stickerEmoji: String) {
    LEFT_ZIGA(StickerPack.FAMILY_PACK, "\uD83D\uDD90"),
    RIGHT_ZIGA(StickerPack.FAMILY_PACK, "\uD83E\uDD1A"),
    SWEET_DREAMS(StickerPack.FAMILY_PACK, "\uD83C\uDF19");
}
