package space.yaroslav.familybot.models

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
    QUOTE_BY_TAG("/quotebytag", 12),
    ROULETTE("/legacy_roulette", 13),
    ASK_WORLD("/ask_world", 14),
    STATS_WORLD("/stats_world", 15),
    ME("/me", 16),
    TOP_HISTORY("/top_history", 17),
    BET("/bet", 18),
    WHATS_MOOD_TODAY("/today", 19),
    BAN("/ban", 20),
    SCENARIO("/play", 21),
    HAMPIK("/hampik", 22),
    ADVANCED_SETTINGS("/advanced_settings", 23),
    STATS_STRIKES("/stats_strikes", 24)
}
