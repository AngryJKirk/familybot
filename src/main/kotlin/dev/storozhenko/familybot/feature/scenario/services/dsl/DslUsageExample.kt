package dev.storozhenko.familybot.feature.scenario.services.dsl

@Suppress("unused")
fun notmain() {
    val imagineWays =
        mutableListOf(
            Way().init {
                answerNumber = 0
                description = "Моего разраба"
                nextMove {
                    description =
                        """Разраб оказался в банде древних укро-турбославян,
                                       | которые готовятся к войне с рептилоидами
                        """.trimMargin()
                    end()
                }
            },
            Way().init {
                answerNumber = 1
                description = "Спасительного Михаила Круга"
                nextMove {
                    description =
                        """Круг оказался иллюзий, которую на тебя наложили рептилоиды.
                        |Их предводитель Виталий приглашает вступить с ними в войну против Укров-Турбославян
                        """.trimMargin()
                    end()
                }
            },
            Way().init {
                answerNumber = 2
                description = "Фестиваль фистинга"
                nextMove {
                    description =
                        """Обманули пидорка на два кулака""".trimMargin()
                    end()
                }
            },
        )
    val scenario = Scenario()
        .init {
            name = "Тест"
            description = "Здарова уебки"
        }
        .entry {
            description = "Представьте"
            way {
                description = "Представил"
                nextMove {
                    description = "Что представил?"
                    ways = imagineWays
                }
            }
            way {
                description = "Не представил"
                nextMove {
                    description = "Что ты не представил?"
                    ways = imagineWays
                }
            }
            way {
                description = "Не хочу + не буду"
                nextMove {
                    description = "Придется представить. Что представил?"
                    ways = imagineWays
                }
            }
        }
    print(ScenarioSqlGenerator().get(scenario))
}
