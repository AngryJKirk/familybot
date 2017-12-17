package space.yaroslav.familybot.route


enum class Command(val command: String) {
    STATS_MONTH("/stats_month"),
    STATS_YEAR("/stats_year"),
    STATS_TOTAL("/stats_total"),
    PIDOR("/pidor"),
    QUOTE("/quote");

}