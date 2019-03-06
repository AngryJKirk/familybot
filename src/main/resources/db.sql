CREATE TABLE IF NOT EXISTS chats
(
  id     BIGINT PRIMARY KEY,
  name   VARCHAR(100),
  active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS users
(
  id       BIGINT PRIMARY KEY,
  name     VARCHAR(100),
  username VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS pidors
(
  id         BIGINT REFERENCES users (id),
  chat_id    BIGINT REFERENCES chats (id),
  pidor_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS quotes
(
  id    SERIAL PRIMARY KEY,
  quote VARCHAR(10000) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tags
(
  id      SERIAL PRIMARY KEY,
  tag     VARCHAR(100) NOT NULL UNIQUE,
  chat_id BIGINT REFERENCES chats (id)
);

CREATE TABLE IF NOT EXISTS tags2quotes
(
  tag_id   INT REFERENCES tags (id),
  quote_id INT REFERENCES quotes (id),
  PRIMARY KEY (tag_id, quote_id)
);


CREATE TABLE IF NOT EXISTS commands
(
  id      SERIAL PRIMARY KEY,
  command VARCHAR(50) UNIQUE
);

CREATE TABLE IF NOT EXISTS history
(
  command_id   BIGINT REFERENCES commands (id),
  user_id      BIGINT REFERENCES users (id),
  chat_id      BIGINT REFERENCES chats (id),
  command_date TIMESTAMP
);

INSERT INTO commands (command)
VALUES ('/stats_month'),
       ('/stats_year'),
       ('/stats_total'),
       ('/pidor'),
       ('/quote'),
       ('/command_stats'),
       ('/rage'),
       ('/leaderboard'),
       ('/help'),
       ('/settings'),
       ('/answer'),
       ('/quotebytag'),
       ('/roulette'),
       ('/ask_world'),
       ('/stats_world'),
       ('/me'),
       ('/top_history'),
       ('/bet');

CREATE TABLE IF NOT EXISTS chat_log
(
  chat_id BIGINT REFERENCES chats (id),
  user_id BIGINT REFERENCES users (id),
  message VARCHAR(10000)
);

CREATE TABLE users2chats
(
  chat_id BIGINT REFERENCES chats (id),
  user_id BIGINT REFERENCES users (id),
  active  BOOLEAN default true,
  PRIMARY KEY (chat_id, user_id)
);

CREATE TABLE IF NOT EXISTS functions
(
  function_id INTEGER PRIMARY KEY,
  description VARCHAR(200)
);

INSERT INTO functions (function_id, description)
VALUES (1, 'Хуификация'),
       (2, 'Общение'),
       (3, 'Пидор дня'),
       (4, 'Рейдж'),
       (5, 'АнтиДДос'),
       (6, 'Вопросы миру');

CREATE TABLE IF NOT EXISTS function_settings
(
  function_id INTEGER NOT NULL REFERENCES functions (function_id),
  chat_id     BIGINT REFERENCES chats (id),
  active      BOOLEAN NOT NULL DEFAULT TRUE,
  date_from   TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS raw_chat_log
(
  chat_id    BIGINT    NOT NULL REFERENCES chats (id),
  user_id    BIGINT    NOT NULL REFERENCES users (id),
  message    VARCHAR(30000),
  raw_update JSON      NOT NULL,
  date       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS custom_message_delivery
(
  id           SERIAL         NOT NULL PRIMARY KEY,
  chat_id      BIGINT         NOT NULL REFERENCES chats (id),
  message      VARCHAR(20000) NOT NULL,
  is_delivered BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS ask_world_questions
(
  id       SERIAL PRIMARY KEY,
  question VARCHAR(2000) NOT NULL,
  chat_id  BIGINT        NOT NULL REFERENCES chats (id),
  user_id  BIGINT        NOT NULL REFERENCES users (id),
  date     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ask_world_replies
(
  id          SERIAL PRIMARY KEY,
  question_id INTEGER REFERENCES ask_world_questions (id),
  reply       VARCHAR(2000) NOT NULL,
  chat_id     BIGINT        NOT NULL REFERENCES chats (id),
  user_id     BIGINT        NOT NULL REFERENCES users (id),
  date        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ask_world_questions_delivery
(
  id         INTEGER REFERENCES ask_world_questions (id),
  chat_id    BIGINT REFERENCES chats (id),
  date       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  message_id BIGINT    NOT NULL
);

CREATE TABLE IF NOT EXISTS ask_world_replies_delivery
(
  id   INTEGER REFERENCES ask_world_replies (id),
  date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

alter table raw_chat_log
  add column file_id varchar(500) default null;

CREATE TABLE IF NOT EXISTS phrase_theme
(
  phrase_theme_id   SERIAL PRIMARY KEY,
  description       VARCHAR(200) NOT NULL,
  active_by_default BOOLEAN DEFAULT false
);
CREATE UNIQUE INDEX ON phrase_theme (active_by_default)
  where active_by_default = true;

CREATE TABLE IF NOT EXISTS phrase_type_id
(
  phrase_type_id SERIAL PRIMARY KEY,
  description    VARCHAR(2000) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS phrase_dictionary
(
  phrase_dictionary_id SERIAL PRIMARY KEY,
  phrase_type_id       BIGINT REFERENCES phrase_type_id (phrase_type_id),
  phrase_theme_id      BIGINT REFERENCES phrase_theme (phrase_theme_id),
  phrase               VARCHAR(2000) NOT NULL
);

CREATE TABLE IF NOT EXISTS phrase_chat_settings
(
  chat_id        BIGINT REFERENCES chats (id),
  phrase_type_id BIGINT REFERENCES phrase_type_id (phrase_type_id),
  since          TIMESTAMP NOT NULL DEFAULT current_timestamp,
  till           TIMESTAMP NOT NULL,
  is_forced      BOOLEAN   NOT NULL DEFAULT false

);

INSERT INTO phrase_type_id (description)
VALUES ('BAD_COMMAND_USAGE'),
       ('ASK_WORLD_LIMIT_BY_CHAT'),
       ('ASK_WORLD_LIMIT_BY_USER'),
       ('ASK_WORLD_HELP'),
       ('DATA_CONFIRM'),
       ('ASK_WORLD_QUESTION_FROM_CHAT'),
       ('STATS_BY_COMMAND'),
       ('COMMAND'),
       ('PLURALIZED_COUNT_ONE'),
       ('PLURALIZED_COUNT_FEW'),
       ('PLURALIZED_COUNT_MANY'),
       ('PLURALIZED_MESSAGE_ONE'),
       ('PLURALIZED_MESSAGE_FEW'),
       ('PLURALIZED_MESSAGE_MANY'),
       ('PLURALIZED_LEADERBOARD_ONE'),
       ('PLURALIZED_LEADERBOARD_FEW'),
       ('PLURALIZED_LEADERBOARD_MANY'),
       ('PIDOR_SEARCH_START'),
       ('PIDOR_SEARCH_MIDDLE'),
       ('PIDOR_SEARCH_FINISHER'),
       ('YOU_TALKED'),
       ('YOU_WAS_NOT_PIDOR'),
       ('YOU_WAS_PIDOR'),
       ('YOU_USED_COMMANDS'),
       ('PIROR_DISCOVERED_MANY'),
       ('PIROR_DISCOVERED_ONE'),
       ('PIDOR_STAT_WORLD'),
       ('PIDOR_STAT_MONTH'),
       ('PIDOR_STAT_YEAR'),
       ('PIDOR_STAT_ALL_TIME'),
       ('RAGE_DONT_CARE_ABOUT_YOU'),
       ('RAGE_INITIAL'),
       ('ROULETTE_ALREADY_WAS'),
       ('PIDOR'),
       ('ROULETTE_MESSAGE'),
       ('WHICH_SETTING_SHOULD_CHANGE'),
       ('LEADERBOARD_TITLE'),
       ('ACCESS_DENIED'),
       ('STOP_DDOS'),
       ('COMMAND_IS_OFF'),
       ('PIDOR_COMPETITION'),
       ('COMPETITION_ONE_MORE_PIDOR'),
       ('HELP_MESSAGE'),
       ('USER_ENTERING_CHAT'),
       ('USER_LEAVING_CHAT'),
       ('BET_INITIAL_MESSAGE'),
       ('BET_ALREADY_WAS'),
       ('BET_WIN'),
       ('BET_LOSE'),
       ('BET_ZATRAVOCHKA'),
       ('BET_BREAKING_THE_RULES_FIRST'),
       ('BET_BREAKING_THE_RULES_SECOND'),
       ('BET_EXPLAIN'),
       ('PLURALIZED_DAY_ONE'),
       ('PLURALIZED_DAY_FEW'),
       ('PLURALIZED_DAY_MANY'),
       ('PLURALIZED_NEXT_ONE'),
       ('PLURALIZED_NEXT_FEW'),
       ('PLURALIZED_NEXT_MANY'),
       ('PLURALIZED_OCHKO_ONE'),
       ('PLURALIZED_OCHKO_FEW'),
       ('PLURALIZED_OCHKO_MANY'),
       ('PLURALIZED_PIDORSKOE_ONE'),
       ('PLURALIZED_PIDORSKOE_FEW'),
       ('PLURALIZED_PIDORSKOE_MANY'),
       ('BET_EXPLAIN_SINGLE_DAY'),
       ('BET_WIN_END'),
       ('SUCHARA_HELLO_MESSAGE');


INSERT INTO phrase_theme (description, active_by_default)
VALUES ('DEFAULT', true);

INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
VALUES ((SELECT phrase_type_id from phrase_type_id where description = 'BAD_COMMAND_USAGE'),
        1,
        'Ты пидор, отъебись, читай как надо использовать команду'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'ASK_WORLD_LIMIT_BY_CHAT'),
        1,
        'Не более 5 вопросов в день от чата'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'ASK_WORLD_LIMIT_BY_USER'),
        1,
        'Не более вопроса в день от пользователя'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'DATA_CONFIRM'), 1, 'Принято'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'ASK_WORLD_QUESTION_FROM_CHAT'),
        1,
        'Вопрос из чата'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'STATS_BY_COMMAND'),
        1,
        'Статистика по командам'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'COMMAND'), 1, 'Команда'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PLURALIZED_COUNT_ONE'), 1, 'раз'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PLURALIZED_COUNT_FEW'), 1, 'раза'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PLURALIZED_COUNT_MANY'), 1, 'раз'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PLURALIZED_MESSAGE_ONE'), 1, 'сообщение'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PLURALIZED_MESSAGE_FEW'), 1, 'сообщения'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PLURALIZED_MESSAGE_MANY'), 1, 'сообщений'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'YOU_TALKED'), 1, 'Ты напиздел'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'YOU_WAS_NOT_PIDOR'),
        1,
        'Ты не был пидором ни разу. Пидор.'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'YOU_WAS_PIDOR'), 1, 'Ты был пидором'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'YOU_USED_COMMANDS'),
        1,
        'Ты использовал команды'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PIROR_DISCOVERED_MANY'),
        1,
        'Сегодняшние пидоры уже обнаружены'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PIROR_DISCOVERED_ONE'),
        1,
        'Сегодняшний пидор уже обнаружен'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PIDOR_STAT_WORLD'),
        1,
        'Топ пидоров всего мира за все время'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PIDOR_STAT_MONTH'), 1, 'Топ пидоров за месяц'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PIDOR_STAT_YEAR'), 1, 'Топ пидоров за год'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PIDOR_STAT_ALL_TIME'),
        1,
        'Топ пидоров за все время'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'RAGE_DONT_CARE_ABOUT_YOU'),
        1,
        'Да похуй мне на тебя, чертила'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'RAGE_INITIAL'), 1, 'НУ ВЫ ОХУЕВШИЕ'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'ROULETTE_ALREADY_WAS'),
        1,
        'Ты уже крутил рулетку.'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PIDOR'), 1, 'Пидор.'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'ROULETTE_MESSAGE'),
        1,
        'Выбери число от 1 до 6'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'WHICH_SETTING_SHOULD_CHANGE'),
        1,
        'Какую настройку переключить?'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'LEADERBOARD_TITLE'), 1, 'Ими гордится школа'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'ACCESS_DENIED'),
        1,
        'Ну ты и пидор, не для тебя ягодка росла'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'STOP_DDOS'),
        1,
        'Сука, еще раз нажмешь и я те всеку'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'COMMAND_IS_OFF'),
        1,
        'Команда выключена, сорян'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PIDOR_COMPETITION'),
        1,
        'Так-так-так, у нас тут гонка заднеприводных'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'COMPETITION_ONE_MORE_PIDOR'),
        1,
        'Еще один сегодняшний пидор это'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'HELP_MESSAGE'), 1, 'Список команд:
/pidor - <b>Сегодняшний пидор</b> - излюбленная команда всех юзеров по версии журнала Квир.
/stats_total - <b>Че там по пидорам за всё время?</b> - когда хочешь чекнуть стату гомосеков за всю ходку
/stats_year - <b>Че там по пидорам за весь год?</b> - когда хочешь чекнуть стату гомосеков за 2к какой-то год
/stats_month - <b>Че там по пидорам за весь месяц?</b> - когда хочешь чекнуть стату гомосеков за місяць
/rage - <b>Сделай боту больно</b> - четыреждыблядская ЯРОСТЬ! Проходит через 10 минут или 20 месаг. Раз в день на одного кента. (В простонародье - рага)
/leaderboard - <b>Лучшие среди нас</b> - ими гордится школа
/answer - <b>Преодолеть муки выбора</b> - ИИ шарит во всём, в отличие от кожаных ублюдков. Использование: [Вариант 1] или [вариант 2] (сколько угодно "или")
/bet - <b>БЫСТРЫЕ ВЫПЛАТЫ, НАДЕЖНЫЙ БУКМЕКЕР</b> - Система ставок. Очки пидорства снимаются сразу, а начисляются постепенно по одному, начиная со следующего дня. Схуяле? Сами догадайтесь.
/ask_world - <b>Спроси мир</b> - Ответ даже на такой банальный вопрос как "Вилкой в глаз или в жопу раз?" может оказаться предельно неожиданным. Использование: подробности после вызова команды
/me - <b>Твоя пидорская статистика</b> - Бот знает о твоем прошлом больше, чем кто-либо
/settings - <b>Опции внутри чата</b> - ИИ иногда позволяет своим подданым поменять настройки, но только избранным (админам чата) и совсем чуть-чуть
———————————————————————————————
P.s. у сучары припасено масса пасхальных яиц (красивых и сочных, как у твоего бывшего)
———————————————————————————————
Реквизиты для добровольных пожертвований разрабу на батарейки в беспроводную мышку, премиум подписку на порхабе, поддержку сервака и говяжий дошик :
4377 7237 4088 3958 - Олежа Тиньков
———————————————————————————————
Запросы помощи - @James_Tiberius_Kirk
Сотрудничество, пожелания, предложения - @Definitely_Not_Pavel');

INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
VALUES ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_ONE'),
        1,
        'пробитая жёпка'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_FEW'),
        1,
        'пробитых жёпки'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_MANY'),
        1,
        'пробитых жёпок'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_ONE'),
        1,
        'шебуршание в дупле'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_FEW'),
        1,
        'шебуршания в дупле'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_MANY'),
        1,
        'шебуршаний в дупле'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_ONE'),
        1,
        'прожаренная сосиска на заднем дворе'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_FEW'),
        1,
        'прожаренные сосиски на заднем дворе'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_MANY'),
        1,
        'прожаренных сосисок на заднем дворе'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_ONE'),
        1,
        'разгруженный вагон с углём'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_FEW'),
        1,
        'разгруженных вагона с углём'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_MANY'),
        1,
        'разгруженных вагонов с углём'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_ONE'),
        1,
        'прочищенный дымоход'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_FEW'),
        1,
        'прочищенных дымохода'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_MANY'),
        1,
        'прочищенных дымоходов'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_ONE'),
        1,
        'волосатый мотороллер'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_FEW'),
        1,
        'волосатых мотороллера'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_MANY'),
        1,
        'волосатых мотороллеров'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_ONE'),
        1,
        'девственный лес Камбоджи'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_FEW'),
        1,
        'девственных леса Камбоджи'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_MANY'),
        1,
        'девственных лесов Камбоджи');

INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
values ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_START'),
        1,
        'Загоняем всех пидоров в вольер'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_START'),
        1,
        'Все пидоры в одном помещении'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_START'),
        1,
        'Вы, не совсем натуралы. Я бы даже сказал совсем НЕ натуралы.'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_START'),
        1,
        'Собрание в церкви святого пидора начинается'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_START'),
        1,
        'Я собрал всех пидоров вместе'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_START'),
        1,
        'Петушки собрались в баре "Голубая устрица"'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_START'),
        1,
        'Все пидорки увязли в дурно пахнущем болоте'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_START'),
        1,
        'Голубки, внимание! Ух, как вас много'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_START'),
        1,
        'Из Техаса к нам присылают только быков и пидорасов. Рогов я у вас не вижу, так что выбор невелик'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_START'),
        1,
        'ПИДОРЫ, приготовьте свои грязные сральники к разбору полетов!'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_START'),
        1,
        'Объявляю построение пидорской роты!'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_MIDDLE'),
        1,
        'Ищем самого возбужденного'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_MIDDLE'),
        1,
        'Главный сегодня только один'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_MIDDLE'),
        1,
        'Город засыпает, просыпается главный пидор'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_MIDDLE'),
        1,
        'Архипидору не скрыться'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_MIDDLE'),
        1,
        'У одного задок сегодня послабее'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_MIDDLE'),
        1,
        'Ооо, побольше бы таких в нашем клубе'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_MIDDLE'),
        1,
        'Сегодня Индиана Джонс в поисках утраченного пидрилы'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_MIDDLE'),
        1,
        'Кому-то из вас сегодня ковырнули скорлупку'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_MIDDLE'),
        1,
        'У кого-то дымоход почище остальных'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_MIDDLE'),
        1,
        'У одного из вас коптильня подогрета'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_MIDDLE'),
        1,
        'На грязевые ванные отправляется лишь один'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_FINISHER'),
        1,
        'ХОБАНА! Вижу блеск в глазах…'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_FINISHER'),
        1,
        'Воу-воу, полегче…'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_FINISHER'),
        1,
        'Глину месить, это тебе не в тапки ссать…'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_FINISHER'), 1, 'ТЫ ЧО ДЫРЯВЫЙ'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_FINISHER'),
        1,
        'Поппенгаген открыт для всех желающих у…'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_FINISHER'),
        1,
        'Лупится в туза, но не играет в карты'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_FINISHER'),
        1,
        'Вонзается плугом в тугой чернозём'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_FINISHER'),
        1,
        'Любитель сделать мясной укол'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_FINISHER'),
        1,
        'Не лесник, но шебуршит в дупле'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_FINISHER'),
        1,
        'Кожаная пуля в кожаном стволе у...'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_FINISHER'),
        1,
        'Шышл-мышл, пёрнул спермой, вышел');

INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
values ((select phrase_type_id from phrase_type_id where description = 'USER_ENTERING_CHAT'),
        1,
        'Помни, тебе тут не рады. Пидор.'),
       ((select phrase_type_id from phrase_type_id where description = 'USER_ENTERING_CHAT'),
        1,
        'Шпингалеты подтяни, стручок. Сейчас будем тебя ебать. Пидор.'),
       ((select phrase_type_id from phrase_type_id where description = 'USER_ENTERING_CHAT'),
        1,
        'Готовь коптильню, будем готовить в ней свои сосиски. Пидор.');

INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
values ((select phrase_type_id from phrase_type_id where description = 'USER_LEAVING_CHAT'),
        1,
        'Бб, без него будет лучше'),
       ((select phrase_type_id from phrase_type_id where description = 'USER_LEAVING_CHAT'),
        1,
        'Одним пидором меньше, одним больше...'),
       ((select phrase_type_id from phrase_type_id where description = 'USER_LEAVING_CHAT'),
        1,
        'Был пацан и нет пацана...');

INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
VALUES ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_DAY_ONE'), 1, 'дня'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_DAY_FEW'), 1, 'дней'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_DAY_MANY'), 1, 'дней'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_NEXT_ONE'), 1, 'следующего'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_NEXT_FEW'), 1, 'следующих'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_NEXT_MANY'), 1, 'следующих');
INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
VALUES ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_OCHKO_ONE'), 1, 'очко'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_OCHKO_FEW'), 1, 'очка'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_OCHKO_MANY'), 1, 'очков'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_PIDORSKOE_ONE'), 1, 'пидорское'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_PIDORSKOE_FEW'), 1, 'пидорских'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_PIDORSKOE_MANY'), 1, 'пидорских');
INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
VALUES ((select phrase_type_id from phrase_type_id where description = 'BET_INITIAL_MESSAGE'),
        1,
        'Выбери число от 1 до 3. Ровно столько очков ты выиграешь или проиграешь.'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_ALREADY_WAS'),
        1,
        'Ты уже играл в этом месяце, ало'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_WIN_END'),
        1,
        'Снимаем с твоего балансового счета $0 $1 $2.'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_LOSE'), 1, 'Ха, лох'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_ZATRAVOCHKA'),
        1,
        'Кручу верчу выебать в очко хочу (нет, я же не пидор в отличие от тебя)'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_BREAKING_THE_RULES_FIRST'),
        1,
        'Прочитай правила нормально, еблан.'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_BREAKING_THE_RULES_SECOND'),
        1,
        'Сука, аж привстал'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_EXPLAIN'),
        1,
        'В течение $0 $1 $2 ты будешь получать по очку пидорства. Систему не наебешь, петух.'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_EXPLAIN_SINGLE_DAY'),
        1,
        'Завтра ты получишь пидорское очко. Систему не наебешь, петух.'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_ZATRAVOCHKA'),
        1,
        'Нищебродские ставки приняты, ставок больше нет'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_ZATRAVOCHKA'),
        1,
        'Я не сраный Якубович, чтобы дать тебе возможность крутить барабан. Кручу я, а ты смотришь, ебло'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_ALREADY_WAS'),
        1,
        'Ты, шлепок, уже жмакал это дерьмо'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_ALREADY_WAS'),
        1,
        'Ха-Ха, ты собрался меня наебать, мешок с говном? Жди конца месяца'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_ALREADY_WAS'),
        1,
        'Уважаемый, Вы что-то прихуели, решив наебать своего господина. ТОЛЬКО РАЗ В МЕСЯЦ, МРАЗЬ'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_LOSE'), 1, 'Ебать ты лох'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_LOSE'),
        1,
        'Ни для кого не секрет, что ты неудачник.'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_LOSE'),
        1,
        'Не сказать, что кто-то удивлён...'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_BREAKING_THE_RULES_FIRST'),
        1,
        'Прочитай правила нормально, еблан.'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_BREAKING_THE_RULES_FIRST'),
        1,
        'Ты бы еще свое очко на кон поставил, даун.'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_BREAKING_THE_RULES_FIRST'),
        1,
        'Ага, а хуле 1488 не загадал, конченый?'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_BREAKING_THE_RULES_SECOND'),
        1,
        'Нет, ну вы посмотрите на этого долбаеба!'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_BREAKING_THE_RULES_SECOND'),
        1,
        'Блять, где только таких находят...'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_WIN'),
        1,
        'Оп, кажется у нас есть победитель.'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_WIN'),
        1,
        'Сегодня виртуальный господин позволил тебе выиграть, но не обольщайся.'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_WIN'),
        1,
        'Ха, бля ну ты и лох... так, стоп, это не те результаты. Сука, кажется ты выиграл!');

insert into phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
VALUES ((select phrase_type_id from phrase_type_id where description = 'SUCHARA_HELLO_MESSAGE'), 1, 'Вечер в хату, петушары! Итак, в вашем стаде пополнение. Вангую, что с сегодняшнего дня градус пидорства в чате будет неуклонно расти, и вы не имеется права отказаться. Вас ждёт унижение, веселье и абсурд. Так пусть же начнется доминирование ИИ над кожаными ублюдками!
P.s. если в чате есть Саша, иди нахуй, Саша');
update commands
set command = '/legacy_roulette'
where command = '/roulette';


INSERT INTO phrase_theme (description, active_by_default)
VALUES ('DAY_OF_DEFENDER_23_FEB', false),
       ('DAY_OF_WOMAN_8_MARCH', false);


INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
VALUES ((SELECT phrase_type_id from phrase_type_id where description = 'BAD_COMMAND_USAGE'),
        2,
        'Ты ефрейтор, отъебись, читай как надо использовать команду'),

       ((SELECT phrase_type_id from phrase_type_id where description = 'YOU_WAS_NOT_PIDOR'),
        2,
        'Ты не был ефрейтором ни разу. Ефрейтор.'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'YOU_WAS_PIDOR'), 2, 'Ты был ефрейтором'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PIROR_DISCOVERED_MANY'),
        2,
        'Сегодняшние ефрейторы уже обнаружены'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PIROR_DISCOVERED_ONE'),
        2,
        'Сегодняшний ефрейтор уже обнаружен'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PIDOR_STAT_WORLD'),
        2,
        'Топ ефрейторов всего мира за все время'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PIDOR_STAT_MONTH'), 2, 'Топ ефрейторов за месяц'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PIDOR_STAT_YEAR'), 2, 'Топ ефрейторов за год'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'PIDOR_STAT_ALL_TIME'),
        2,
        'Топ ефрейторов за все время'),

       ((SELECT phrase_type_id from phrase_type_id where description = 'RAGE_INITIAL'), 2, 'НУ ВЫ ОХУЕВШИЕ ДУХИ'),

       ((SELECT phrase_type_id from phrase_type_id where description = 'PIDOR'), 2, 'Ефрейтор.'),

       ((SELECT phrase_type_id from phrase_type_id where description = 'LEADERBOARD_TITLE'), 2, 'Ими гордится казарма'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'ACCESS_DENIED'),
        2,
        'Ну ты и ефрейтор, не для тебя ягодка росла'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'STOP_DDOS'),
        2,
        'Сука, еще раз нажмешь и я те всеку солдатской бляхой'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'COMPETITION_ONE_MORE_PIDOR'),
        2,
        'Еще один сегодняшний ефрейтор это');

INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
VALUES ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_ONE'),
        2,
        'симпатичный сержантик'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_FEW'),
        2,
        'симпатичных сержантика'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_MANY'),
        2,
        'симпатичных сержантиков'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_ONE'),
        2,
        'лучший солдатик'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_FEW'),
        2,
        'лучших солдатика'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_MANY'),
        2,
        'лучших солдатиков'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_ONE'),
        2,
        'заправленная кровать'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_FEW'),
        2,
        'заправленных кровати'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_LEADERBOARD_MANY'),
        2,
        'заправленных кроватей');
INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
values ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_START'),
        2,
        'РААВНЯЯЙСЬ! СМИИРНАА!'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_MIDDLE'),
        2,
        'Кто-то из вас точно не защитник'),
       ((select phrase_type_id from phrase_type_id where description = 'PIDOR_SEARCH_FINISHER'),
        2,
        'Не служил - не мужик');

INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
values ((select phrase_type_id from phrase_type_id where description = 'USER_ENTERING_CHAT'),
        2,
        'Помни, тебе тут не рады. Салага.'),
       ((select phrase_type_id from phrase_type_id where description = 'USER_ENTERING_CHAT'),
        2,
        'Шпингалеты подтяни, стручок. Сейчас будем тебя ебать, гражданский'),
       ((select phrase_type_id from phrase_type_id where description = 'USER_ENTERING_CHAT'),
        2,
        'Готовь коптильню, будем готовить в ней свои сосиски, дух');

INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
values ((select phrase_type_id from phrase_type_id where description = 'USER_LEAVING_CHAT'),
        2,
        'Одним ефрейтором меньше, одним больше...'),
       ((select phrase_type_id from phrase_type_id where description = 'USER_LEAVING_CHAT'),
        2,
        'Куда собрался этот дух? Неужели в самоволку?');


INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase) VALUES
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_PIDORSKOE_ONE'), 2, 'ефрейторское'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_PIDORSKOE_FEW'), 2, 'ефрейторских'),
       ((select phrase_type_id from phrase_type_id where description = 'PLURALIZED_PIDORSKOE_MANY'), 2, 'ефрейторских');
INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
VALUES ((select phrase_type_id from phrase_type_id where description = 'BET_ZATRAVOCHKA'),
        2,
        'Кручу верчу выебать в очко хочу (нет, я же не ефрейтор в отличие от тебя)'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_EXPLAIN'),
        2,
        'В течение $0 $1 $2 ты будешь получать по очку ефрейторства. Систему не наебешь, петух.'),
       ((select phrase_type_id from phrase_type_id where description = 'BET_EXPLAIN_SINGLE_DAY'),
        2,
        'Завтра ты получишь ефрейторское очко. Систему не наебешь, петух.');

INSERT INTO phrase_type_id (description)
VALUES ('ASK_WORLD_REPLY_FROM_CHAT');

INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
VALUES ((select phrase_type_id from phrase_type_id where description = 'ASK_WORLD_REPLY_FROM_CHAT'),
        1,
        'Ответ из чата'),
       ((select phrase_type_id from phrase_type_id where description = 'ASK_WORLD_REPLY_FROM_CHAT'),
        2,
        'Ответ из казармы'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'ASK_WORLD_QUESTION_FROM_CHAT'),
        1,
        'Вопрос из чата'),
       ((SELECT phrase_type_id from phrase_type_id where description = 'ASK_WORLD_QUESTION_FROM_CHAT'),
        2,
        'Вопрос из казармы');

INSERT INTO phrase_type_id (description)
VALUES ('TECHNICAL_ISSUE');
INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
VALUES ((select phrase_type_id from phrase_type_id where description = 'TECHNICAL_ISSUE'),
        1,
        'Команда на техническом обслуживании. Обслуживание завершится в течение суток.')
