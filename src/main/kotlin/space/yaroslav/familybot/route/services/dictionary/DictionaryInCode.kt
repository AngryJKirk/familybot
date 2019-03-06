package space.yaroslav.familybot.route.services.dictionary

import space.yaroslav.familybot.route.models.Phrase

class DictionaryInCode : Dictionary {
    override fun getAll(phrase: Phrase): List<String> {
        TODO("not implemented")
    }

    override fun get(phrase: Phrase): String {
        return when (phrase) {
            Phrase.BAD_COMMAND_USAGE -> "Ты пидор, отъебись, читай как надо использовать команду"
            Phrase.ASK_WORLD_LIMIT_BY_CHAT -> "Не более 5 вопросов в день от чата"
            Phrase.ASK_WORLD_LIMIT_BY_USER -> "Не более вопроса в день от пользователя"
            Phrase.DATA_CONFIRM -> "Принято"
            Phrase.ASK_WORLD_QUESTION_FROM_CHAT -> "Вопрос из чата"
            Phrase.STATS_BY_COMMAND -> "Статистика по командам"
            Phrase.COMMAND -> "Команда"
            Phrase.PLURALIZED_COUNT_ONE -> "раз"
            Phrase.PLURALIZED_COUNT_FEW -> "раза"
            Phrase.PLURALIZED_COUNT_MANY -> "раз"
            Phrase.PLURALIZED_MESSAGE_ONE -> "сообщение"
            Phrase.PLURALIZED_MESSAGE_FEW -> "сообщения"
            Phrase.PLURALIZED_MESSAGE_MANY -> "сообщений"
            Phrase.YOU_TALKED -> "Ты напиздел"
            Phrase.YOU_WAS_NOT_PIDOR -> "Ты не был пидором ни разу. Пидор."
            Phrase.YOU_WAS_PIDOR -> "Ты был пидором"
            Phrase.YOU_USED_COMMANDS -> "Ты использовал команды"
            Phrase.PIROR_DISCOVERED_MANY -> "Сегодняшние пидоры уже обнаружены"
            Phrase.PIROR_DISCOVERED_ONE -> "Сегодняшний пидор уже обнаружен"
            Phrase.PIDOR_STAT_WORLD -> "Топ пидоров всего мира за все время"
            Phrase.PIDOR_STAT_MONTH -> "Топ пидоров за месяц"
            Phrase.PIDOR_STAT_YEAR -> "Топ пидоров за год"
            Phrase.PIDOR_STAT_ALL_TIME -> "Топ пидоров за все время"
            Phrase.RAGE_DONT_CARE_ABOUT_YOU -> "Да похуй мне на тебя, чертила"
            Phrase.RAGE_INITIAL -> "НУ ВЫ ОХУЕВШИЕ"
            Phrase.ROULETTE_ALREADY_WAS -> "Ты уже крутил рулетку."
            Phrase.PIDOR -> "Пидор."
            Phrase.ROULETTE_MESSAGE -> "Выбери число от 1 до 6"
            Phrase.WHICH_SETTING_SHOULD_CHANGE -> "Какую настройку переключить?"
            Phrase.LEADERBOARD_TITLE -> "Ими гордится школа"
            Phrase.ACCESS_DENIED -> "Ну ты и пидор, не для тебя ягодка росла"
            Phrase.STOP_DDOS -> "Сука, еще раз нажмешь и я те всеку"
            Phrase.COMMAND_IS_OFF -> "Команда выключена, сорян"
            Phrase.PIDOR_COMPETITION -> "Так-так-так, у нас тут гонка заднеприводных"
            Phrase.COMPETITION_ONE_MORE_PIDOR -> "Еще один сегодняшний пидор это"
            Phrase.HELP_MESSAGE -> "help"
            Phrase.ASK_WORLD_HELP -> """
        Данная команда позволяет вам задать вопрос всем остальным чатам, где есть этот бот.
        Использование: /ask_world <вопрос>
        Если вам придет вопрос, то нужно ответить на него, в таком случае ответ отправится в чат, где он был задан.
        Ответить можно лишь один раз от человека.
        Лимиты: не более одного вопроса от человека в день, не более 5 вопросов от чата в день.
        Команда работает в тестовом режиме. В настройках можно отключить ее, тогда вам не будут приходить вопросы и вы сами не сможете их задавать.
    """
            Phrase.PLURALIZED_LEADERBOARD_ONE -> TODO()
            Phrase.PLURALIZED_LEADERBOARD_FEW -> TODO()
            Phrase.PLURALIZED_LEADERBOARD_MANY -> TODO()
            Phrase.PIDOR_SEARCH_START -> TODO()
            Phrase.PIDOR_SEARCH_MIDDLE -> TODO()
            Phrase.PIDOR_SEARCH_FINISHER -> TODO()
            Phrase.USER_ENTERING_CHAT -> TODO()
            Phrase.USER_LEAVING_CHAT -> TODO()
            Phrase.BET_INITIAL_MESSAGE -> TODO()
            Phrase.BET_ALREADY_WAS -> TODO()
            Phrase.BET_WIN -> TODO()
            Phrase.BET_LOSE -> TODO()
            Phrase.BET_ZATRAVOCHKA -> TODO()
            Phrase.BET_BREAKING_THE_RULES_FIRST -> TODO()
            Phrase.BET_BREAKING_THE_RULES_SECOND -> TODO()
            Phrase.BET_EXPLAIN -> TODO()
            Phrase.PLURALIZED_DAY_ONE -> TODO()
            Phrase.PLURALIZED_DAY_FEW -> TODO()
            Phrase.PLURALIZED_DAY_MANY -> TODO()
            Phrase.PLURALIZED_NEXT_ONE -> TODO()
            Phrase.PLURALIZED_NEXT_FEW -> TODO()
            Phrase.PLURALIZED_NEXT_MANY -> TODO()
            Phrase.PLURALIZED_OCHKO_ONE -> TODO()
            Phrase.PLURALIZED_OCHKO_FEW -> TODO()
            Phrase.PLURALIZED_OCHKO_MANY -> TODO()
            Phrase.PLURALIZED_PIDORSKOE_ONE -> TODO()
            Phrase.PLURALIZED_PIDORSKOE_FEW -> TODO()
            Phrase.PLURALIZED_PIDORSKOE_MANY -> TODO()
            Phrase.BET_EXPLAIN_SINGLE_DAY -> TODO()
            Phrase.BET_WIN_END -> TODO()
            Phrase.SUCHARA_HELLO_MESSAGE -> TODO()
            Phrase.ASK_WORLD_REPLY_FROM_CHAT -> TODO()
            Phrase.TECHNICAL_ISSUE -> TODO()
        }
    }
}
