package space.yaroslav.familybot.route.models


enum class Command(val command: String, val id: Int) {
    STATS_MONTH("/stats_month", 1),
    STATS_YEAR("/stats_year", 2),
    STATS_TOTAL("/stats_total", 3),
    PIDOR("/pidor", 4),
    QUOTE("/quote", 5),
    COMMAND_STATS("/command_stats", 6),
    RAGE("/rage", 7),
    LEADERBOARD("/leaderboard", 8),
    HELP("/help", 9),
    SETTINGS("/settings", 10),
    ANSWER("/answer", 11),
    QUOTE_BY_TAG("/quotebytag", 12);



}

