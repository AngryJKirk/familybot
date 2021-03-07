CREATE EXTENSION IF NOT EXISTS pgcrypto;

------------------- TABLES ----------------------
CREATE TABLE IF NOT EXISTS bans_entity_types
(
    entity_type_id     INTEGER       NOT NULL,
    entity_description VARCHAR(1000) NOT NULL,
    CONSTRAINT bans_entity_types_pkey
        PRIMARY KEY (entity_type_id)
);

CREATE TABLE IF NOT EXISTS bans
(
    ban_uuid        UUID                    NOT NULL,
    ban_date        TIMESTAMP DEFAULT NOW() NOT NULL,
    ban_till_date   TIMESTAMP               NOT NULL,
    ban_description VARCHAR(2000)           NOT NULL,
    entity_id       BIGINT                  NOT NULL,
    entity_type_id  INTEGER                 NOT NULL,
    CONSTRAINT bans_pkey
        PRIMARY KEY (ban_uuid),
    CONSTRAINT bans_entity_type_id_fkey
        FOREIGN KEY (entity_type_id) REFERENCES bans_entity_types
);

CREATE TABLE IF NOT EXISTS chats
(
    id     BIGINT               NOT NULL,
    name   VARCHAR(400),
    active BOOLEAN DEFAULT TRUE NOT NULL,
    CONSTRAINT chats_pkey
        PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS commands
(
    id      SERIAL NOT NULL,
    command VARCHAR(50),
    CONSTRAINT commands_pkey
        PRIMARY KEY (id),
    CONSTRAINT commands_command_key
        UNIQUE (command)
);

CREATE TABLE IF NOT EXISTS custom_message_delivery
(
    id           SERIAL         NOT NULL,
    chat_id      BIGINT         NOT NULL,
    message      VARCHAR(20000) NOT NULL,
    is_delivered BOOLEAN DEFAULT FALSE,
    CONSTRAINT custom_message_delivery_pkey
        PRIMARY KEY (id),
    CONSTRAINT custom_message_delivery_chat_id_fkey
        FOREIGN KEY (chat_id) REFERENCES chats
);

CREATE TABLE IF NOT EXISTS functions
(
    function_id INTEGER NOT NULL,
    description VARCHAR(200),
    CONSTRAINT functions_pkey
        PRIMARY KEY (function_id)
);

CREATE TABLE IF NOT EXISTS function_settings
(
    function_id INTEGER                NOT NULL,
    chat_id     BIGINT,
    active      BOOLEAN   DEFAULT TRUE NOT NULL,
    date_from   TIMESTAMP DEFAULT NOW(),
    id          SERIAL                 NOT NULL,
    CONSTRAINT function_settings_pkey
        PRIMARY KEY (id),
    CONSTRAINT function_settings_chat_id_fkey
        FOREIGN KEY (chat_id) REFERENCES chats,
    CONSTRAINT function_settings_function_id_fkey
        FOREIGN KEY (function_id) REFERENCES functions
);

CREATE TABLE IF NOT EXISTS phrase_theme
(
    phrase_theme_id   SERIAL       NOT NULL,
    description       VARCHAR(200) NOT NULL,
    active_by_default BOOLEAN DEFAULT FALSE,
    CONSTRAINT phrase_theme_pkey
        PRIMARY KEY (phrase_theme_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS phrase_theme_active_by_default_idx
    ON phrase_theme (active_by_default)
    WHERE (active_by_default = TRUE);

CREATE TABLE IF NOT EXISTS phrase_theme_settings
(
    phrase_theme_id BIGINT,
    since           TIMESTAMP NOT NULL,
    till            TIMESTAMP NOT NULL,
    CONSTRAINT phrase_theme_settings_phrase_theme_id_fkey
        FOREIGN KEY (phrase_theme_id) REFERENCES phrase_theme
);

CREATE TABLE IF NOT EXISTS phrase_type_id
(
    phrase_type_id SERIAL        NOT NULL,
    description    VARCHAR(2000) NOT NULL,
    CONSTRAINT phrase_type_id_pkey
        PRIMARY KEY (phrase_type_id),
    CONSTRAINT phrase_type_id_description_key
        UNIQUE (description)
);

CREATE TABLE IF NOT EXISTS phrase_chat_settings
(
    chat_id        BIGINT,
    phrase_type_id BIGINT,
    since          TIMESTAMP DEFAULT NOW() NOT NULL,
    till           TIMESTAMP               NOT NULL,
    is_forced      BOOLEAN   DEFAULT FALSE NOT NULL,
    CONSTRAINT phrase_chat_settings_chat_id_fkey
        FOREIGN KEY (chat_id) REFERENCES chats,
    CONSTRAINT phrase_chat_settings_phrase_type_id_fkey
        FOREIGN KEY (phrase_type_id) REFERENCES phrase_type_id
);

CREATE TABLE IF NOT EXISTS phrase_dictionary
(
    phrase_dictionary_id SERIAL        NOT NULL,
    phrase_type_id       BIGINT,
    phrase_theme_id      BIGINT,
    phrase               VARCHAR(4000) NOT NULL,
    CONSTRAINT phrase_dictionary_pkey
        PRIMARY KEY (phrase_dictionary_id),
    CONSTRAINT phrase_dictionary_phrase_theme_id_fkey
        FOREIGN KEY (phrase_theme_id) REFERENCES phrase_theme,
    CONSTRAINT phrase_dictionary_phrase_type_id_fkey
        FOREIGN KEY (phrase_type_id) REFERENCES phrase_type_id
);

CREATE TABLE IF NOT EXISTS quotes
(
    id    SERIAL         NOT NULL,
    quote VARCHAR(10000) NOT NULL,
    CONSTRAINT quotes_pkey
        PRIMARY KEY (id),
    CONSTRAINT quotes_quote_key
        UNIQUE (quote)
);

CREATE TABLE IF NOT EXISTS tags
(
    id      SERIAL       NOT NULL,
    tag     VARCHAR(100) NOT NULL,
    chat_id BIGINT,
    CONSTRAINT tags_pkey
        PRIMARY KEY (id),
    CONSTRAINT tags_chats_id_fk
        FOREIGN KEY (chat_id) REFERENCES chats
);

CREATE UNIQUE INDEX IF NOT EXISTS title_idx
    ON tags (tag);

CREATE TABLE IF NOT EXISTS tags2quotes
(
    tag_id   INTEGER NOT NULL,
    quote_id INTEGER NOT NULL,
    CONSTRAINT tags2quotes_pkey
        PRIMARY KEY (tag_id, quote_id),
    CONSTRAINT tags2quotes_quote_id_fkey
        FOREIGN KEY (quote_id) REFERENCES quotes,
    CONSTRAINT tags2quotes_tag_id_fkey
        FOREIGN KEY (tag_id) REFERENCES tags
);

CREATE TABLE IF NOT EXISTS users
(
    id       BIGINT NOT NULL,
    name     VARCHAR(100),
    username VARCHAR(100),
    CONSTRAINT users_pkey
        PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ask_world_questions
(
    id       SERIAL                  NOT NULL,
    question VARCHAR(2000)           NOT NULL,
    chat_id  BIGINT                  NOT NULL,
    user_id  BIGINT                  NOT NULL,
    date     TIMESTAMP DEFAULT NOW() NOT NULL,
    CONSTRAINT ask_world_questions_pkey
        PRIMARY KEY (id),
    CONSTRAINT ask_world_questions_chat_id_fkey
        FOREIGN KEY (chat_id) REFERENCES chats,
    CONSTRAINT ask_world_questions_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users
);

CREATE TABLE IF NOT EXISTS ask_world_questions_delivery
(
    id         INTEGER,
    chat_id    BIGINT,
    date       TIMESTAMP DEFAULT NOW() NOT NULL,
    message_id BIGINT                  NOT NULL,
    CONSTRAINT ask_world_questions_delivery_chat_id_fkey
        FOREIGN KEY (chat_id) REFERENCES chats,
    CONSTRAINT ask_world_questions_delivery_id_fkey
        FOREIGN KEY (id) REFERENCES ask_world_questions
);

CREATE TABLE IF NOT EXISTS ask_world_replies
(
    id          SERIAL                  NOT NULL,
    question_id INTEGER,
    reply       VARCHAR(20000)          NOT NULL,
    chat_id     BIGINT                  NOT NULL,
    user_id     BIGINT                  NOT NULL,
    date        TIMESTAMP DEFAULT NOW() NOT NULL,
    CONSTRAINT ask_world_replies_pkey
        PRIMARY KEY (id),
    CONSTRAINT ask_world_replies_chat_id_fkey
        FOREIGN KEY (chat_id) REFERENCES chats,
    CONSTRAINT ask_world_replies_question_id_fkey
        FOREIGN KEY (question_id) REFERENCES ask_world_questions,
    CONSTRAINT ask_world_replies_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users
);

CREATE TABLE IF NOT EXISTS ask_world_replies_delivery
(
    id   INTEGER,
    date TIMESTAMP DEFAULT NOW() NOT NULL,
    CONSTRAINT ask_world_replies_delivery_id_fkey
        FOREIGN KEY (id) REFERENCES ask_world_replies
);

CREATE TABLE IF NOT EXISTS chat_log
(
    chat_id BIGINT,
    user_id BIGINT,
    message VARCHAR(10000),
    id      SERIAL NOT NULL,
    CONSTRAINT chat_log_pkey
        PRIMARY KEY (id),
    CONSTRAINT chat_log_chat_id_fkey
        FOREIGN KEY (chat_id) REFERENCES chats,
    CONSTRAINT user_id
        FOREIGN KEY (user_id) REFERENCES users
);

CREATE TABLE IF NOT EXISTS history
(
    command_id   BIGINT,
    user_id      BIGINT,
    chat_id      BIGINT,
    command_date TIMESTAMP,
    CONSTRAINT history_chat_id_fkey
        FOREIGN KEY (chat_id) REFERENCES chats,
    CONSTRAINT history_command_id_fkey
        FOREIGN KEY (command_id) REFERENCES commands,
    CONSTRAINT history_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users
);

CREATE TABLE IF NOT EXISTS pidors
(
    id         BIGINT,
    pidor_date TIMESTAMP,
    chat_id    BIGINT,
    CONSTRAINT pidors_chats_id_fk
        FOREIGN KEY (chat_id) REFERENCES chats,
    CONSTRAINT pidors_id_fkey
        FOREIGN KEY (id) REFERENCES users
);

CREATE TABLE IF NOT EXISTS raw_chat_log
(
    chat_id    BIGINT                  NOT NULL,
    user_id    BIGINT                  NOT NULL,
    message    VARCHAR(30000),
    raw_update JSON                    NOT NULL,
    date       TIMESTAMP DEFAULT NOW() NOT NULL,
    file_id    VARCHAR(500),
    CONSTRAINT raw_chat_log_chat_id_fkey
        FOREIGN KEY (chat_id) REFERENCES chats,
    CONSTRAINT raw_chat_log_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users
);

CREATE INDEX IF NOT EXISTS idx_raw_chat_log_chat_id
    ON raw_chat_log (chat_id);

CREATE INDEX IF NOT EXISTS raw_chat_log_date_idx
    ON raw_chat_log (date);

CREATE TABLE IF NOT EXISTS users2chats
(
    chat_id BIGINT               NOT NULL,
    user_id BIGINT               NOT NULL,
    active  BOOLEAN DEFAULT TRUE NOT NULL,
    CONSTRAINT users2chats_chat_id_user_id_pk
        PRIMARY KEY (chat_id, user_id),
    CONSTRAINT users2chats_chat_id_fkey
        FOREIGN KEY (chat_id) REFERENCES chats,
    CONSTRAINT users2chats_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users
);

CREATE TABLE IF NOT EXISTS scenario_move
(
    move_id                   UUID    DEFAULT gen_random_uuid() NOT NULL,
    scenario_move_description VARCHAR(1000),
    is_the_end                BOOLEAN DEFAULT FALSE             NOT NULL,
    CONSTRAINT scenario_move_pkey
        PRIMARY KEY (move_id)
);

CREATE TABLE IF NOT EXISTS scenario_way
(
    way_id                   UUID DEFAULT gen_random_uuid() NOT NULL,
    answer_number            INTEGER                        NOT NULL,
    scenario_way_description VARCHAR(500),
    next_move_id             UUID,
    CONSTRAINT scenario_way_pkey
        PRIMARY KEY (way_id),
    CONSTRAINT scenario_way_next_move_id_fkey
        FOREIGN KEY (next_move_id) REFERENCES scenario_move
);

CREATE TABLE IF NOT EXISTS move2way
(
    id      UUID DEFAULT gen_random_uuid() NOT NULL,
    way_id  UUID,
    move_id UUID,
    CONSTRAINT move2way_pkey
        PRIMARY KEY (id),
    CONSTRAINT move2way_way_id_fkey
        FOREIGN KEY (way_id) REFERENCES scenario_way,
    CONSTRAINT move2way_move_id_fkey
        FOREIGN KEY (move_id) REFERENCES scenario_move
);

CREATE TABLE IF NOT EXISTS scenario
(
    scenario_id          UUID DEFAULT gen_random_uuid() NOT NULL,
    scenario_description VARCHAR(1000),
    scenario_name        VARCHAR(100),
    entry_move           UUID,
    CONSTRAINT scenario_pkey
        PRIMARY KEY (scenario_id),
    CONSTRAINT scenario_entry_move_fkey
        FOREIGN KEY (entry_move) REFERENCES scenario_move
);

CREATE TABLE IF NOT EXISTS scenario_poll
(
    poll_id          VARCHAR(100) NOT NULL,
    chat_id          BIGINT       NOT NULL,
    create_date      TIMESTAMP    NOT NULL,
    scenario_move_id UUID,
    poll_message_id  BIGINT       NOT NULL,
    CONSTRAINT scenario_poll_pkey
        PRIMARY KEY (poll_id),
    CONSTRAINT scenario_poll_chat_id_fkey
        FOREIGN KEY (chat_id) REFERENCES chats,
    CONSTRAINT scenario_poll_scenario_move_id_fkey
        FOREIGN KEY (scenario_move_id) REFERENCES scenario_move
);

CREATE TABLE IF NOT EXISTS scenario_choices
(
    choice_id       UUID      DEFAULT gen_random_uuid() NOT NULL,
    user_id         BIGINT,
    chat_id         BIGINT,
    choice_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    scenario_way_id UUID,
    CONSTRAINT scenario_choices_pkey
        PRIMARY KEY (choice_id),
    CONSTRAINT scenario_choices_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users,
    CONSTRAINT scenario_choices_chat_id_fkey
        FOREIGN KEY (chat_id) REFERENCES chats,
    CONSTRAINT scenario_choices_scenario_way_id_fkey
        FOREIGN KEY (scenario_way_id) REFERENCES scenario_way
);

CREATE TABLE IF NOT EXISTS scenario_states
(
    state_id         UUID DEFAULT gen_random_uuid() NOT NULL,
    state_date       TIMESTAMP                      NOT NULL,
    chat_id          BIGINT,
    scenario_move_id UUID,
    CONSTRAINT scenario_states_pkey
        PRIMARY KEY (state_id),
    CONSTRAINT scenario_states_chat_id_fkey
        FOREIGN KEY (chat_id) REFERENCES chats,
    CONSTRAINT scenario_states_scenario_move_id_fkey
        FOREIGN KEY (scenario_move_id) REFERENCES scenario_move
);


------------------- DATA ----------------------
INSERT INTO commands (id, command)
VALUES (1, '/stats_month'),
       (2, '/stats_year'),
       (3, '/stats_total'),
       (4, '/pidor'),
       (5, '/quote'),
       (6, '/command_stats'),
       (7, '/rage'),
       (8, '/leaderboard'),
       (9, '/help'),
       (10, '/settings'),
       (11, '/answer'),
       (12, '/quotebytag'),
       (13, '/legacy_roulette'),
       (14, '/ask_world'),
       (15, '/stats_world'),
       (16, '/me'),
       (17, '/top_history'),
       (18, '/bet'),
       (19, '/today'),
       (20, '/ban'),
       (21, '/play'),
       (22, '/hampik'),
       (23, '/advanced_settings')
;

INSERT INTO functions (function_id, description)
VALUES (1, 'Хуификация'),
       (2, 'Общение'),
       (3, 'Пидор дня'),
       (4, 'Рейдж'),
       (5, 'АнтиДДос'),
       (6, 'Вопросы миру'),
       (7, 'Приветствия и прощания'),
       (8, 'Реакция на обращения')
;

INSERT INTO phrase_type_id (phrase_type_id, description)
VALUES (1, 'BAD_COMMAND_USAGE'),
       (2, 'ASK_WORLD_LIMIT_BY_CHAT'),
       (3, 'ASK_WORLD_LIMIT_BY_USER'),
       (4, 'ASK_WORLD_HELP'),
       (5, 'DATA_CONFIRM'),
       (6, 'ASK_WORLD_QUESTION_FROM_CHAT'),
       (7, 'STATS_BY_COMMAND'),
       (8, 'COMMAND'),
       (9, 'PLURALIZED_COUNT_ONE'),
       (10, 'PLURALIZED_COUNT_FEW'),
       (11, 'PLURALIZED_COUNT_MANY'),
       (12, 'PLURALIZED_MESSAGE_ONE'),
       (13, 'PLURALIZED_MESSAGE_FEW'),
       (14, 'PLURALIZED_MESSAGE_MANY'),
       (15, 'PLURALIZED_LEADERBOARD_ONE'),
       (16, 'PLURALIZED_LEADERBOARD_FEW'),
       (17, 'PLURALIZED_LEADERBOARD_MANY'),
       (18, 'PIDOR_SEARCH_START'),
       (19, 'PIDOR_SEARCH_MIDDLE'),
       (20, 'PIDOR_SEARCH_FINISHER'),
       (21, 'YOU_TALKED'),
       (22, 'YOU_WAS_NOT_PIDOR'),
       (23, 'YOU_WAS_PIDOR'),
       (24, 'YOU_USED_COMMANDS'),
       (25, 'PIROR_DISCOVERED_MANY'),
       (26, 'PIROR_DISCOVERED_ONE'),
       (27, 'PIDOR_STAT_WORLD'),
       (28, 'PIDOR_STAT_MONTH'),
       (29, 'PIDOR_STAT_YEAR'),
       (30, 'PIDOR_STAT_ALL_TIME'),
       (31, 'RAGE_DONT_CARE_ABOUT_YOU'),
       (32, 'RAGE_INITIAL'),
       (33, 'ROULETTE_ALREADY_WAS'),
       (34, 'PIDOR'),
       (35, 'ROULETTE_MESSAGE'),
       (36, 'WHICH_SETTING_SHOULD_CHANGE'),
       (37, 'LEADERBOARD_TITLE'),
       (38, 'ACCESS_DENIED'),
       (39, 'STOP_DDOS'),
       (40, 'COMMAND_IS_OFF'),
       (41, 'PIDOR_COMPETITION'),
       (42, 'COMPETITION_ONE_MORE_PIDOR'),
       (43, 'HELP_MESSAGE'),
       (44, 'USER_ENTERING_CHAT'),
       (45, 'USER_LEAVING_CHAT'),
       (46, 'BET_INITIAL_MESSAGE'),
       (47, 'BET_ALREADY_WAS'),
       (48, 'BET_WIN'),
       (49, 'BET_LOSE'),
       (50, 'BET_ZATRAVOCHKA'),
       (51, 'BET_BREAKING_THE_RULES_FIRST'),
       (52, 'BET_BREAKING_THE_RULES_SECOND'),
       (53, 'BET_EXPLAIN'),
       (54, 'PLURALIZED_DAY_ONE'),
       (55, 'PLURALIZED_DAY_FEW'),
       (56, 'PLURALIZED_DAY_MANY'),
       (57, 'PLURALIZED_NEXT_ONE'),
       (58, 'PLURALIZED_NEXT_FEW'),
       (59, 'PLURALIZED_NEXT_MANY'),
       (60, 'PLURALIZED_OCHKO_ONE'),
       (61, 'PLURALIZED_OCHKO_FEW'),
       (62, 'PLURALIZED_OCHKO_MANY'),
       (63, 'PLURALIZED_PIDORSKOE_ONE'),
       (64, 'PLURALIZED_PIDORSKOE_FEW'),
       (65, 'PLURALIZED_PIDORSKOE_MANY'),
       (66, 'BET_EXPLAIN_SINGLE_DAY'),
       (67, 'BET_WIN_END'),
       (68, 'SUCHARA_HELLO_MESSAGE'),
       (69, 'ASK_WORLD_REPLY_FROM_CHAT'),
       (70, 'TECHNICAL_ISSUE'),
       (71, 'BET_WINNABLE_NUMBERS_ANNOUNCEMENT'),
       (72, 'LEADERBOARD_NONE'),
       (73, 'ADVANCED_SETTINGS'),
       (74, 'ADVANCED_SETTINGS_ERROR'),
       (75, 'ADVANCED_SETTINGS_OK')
;

INSERT INTO phrase_theme (description, active_by_default)
VALUES ('DEFAULT', TRUE),
       ('DAY_OF_DEFENDER_23_FEB', FALSE),
       ('DAY_OF_WOMAN_8_MARCH', FALSE);



INSERT INTO phrase_dictionary (phrase_type_id, phrase_theme_id, phrase)
VALUES ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BAD_COMMAND_USAGE'),
        1,
        'Ты пидор, отъебись, читай как надо использовать команду'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ASK_WORLD_LIMIT_BY_CHAT'), 1,
        'Не более 5 вопросов в день от чата'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ASK_WORLD_LIMIT_BY_USER'), 1,
        'Не более вопроса в день от пользователя'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'DATA_CONFIRM'), 1, 'Принято'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ASK_WORLD_QUESTION_FROM_CHAT'), 1,
        'Вопрос из чата'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'STATS_BY_COMMAND'), 1,
        'Статистика по командам'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'COMMAND'), 1, 'Команда'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_COUNT_ONE'), 1, 'раз'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_COUNT_FEW'), 1, 'раза'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_COUNT_MANY'), 1, 'раз'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_MESSAGE_ONE'), 1, 'сообщение'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_MESSAGE_FEW'), 1, 'сообщения'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_MESSAGE_MANY'), 1, 'сообщений'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'YOU_TALKED'), 1, 'Ты напиздел'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'YOU_WAS_NOT_PIDOR'), 1,
        'Ты не был пидором ни разу. Пидор.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'YOU_WAS_PIDOR'), 1, 'Ты был пидором'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'YOU_USED_COMMANDS'), 1,
        'Ты использовал команды'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIROR_DISCOVERED_MANY'), 1,
        'Сегодняшние пидоры уже обнаружены'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIROR_DISCOVERED_ONE'), 1,
        'Сегодняшний пидор уже обнаружен'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_STAT_WORLD'), 1,
        'Топ пидоров всего мира за все время'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_STAT_MONTH'), 1, 'Топ пидоров за месяц'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_STAT_YEAR'), 1, 'Топ пидоров за год'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_STAT_ALL_TIME'), 1,
        'Топ пидоров за все время'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'RAGE_DONT_CARE_ABOUT_YOU'), 1,
        'Да похуй мне на тебя, чертила'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'RAGE_INITIAL'), 1, 'НУ ВЫ ОХУЕВШИЕ'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ROULETTE_ALREADY_WAS'), 1,
        'Ты уже крутил рулетку.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR'), 1, 'Пидор.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ROULETTE_MESSAGE'), 1,
        'Выбери число от 1 до 6'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'WHICH_SETTING_SHOULD_CHANGE'), 1,
        'Какую настройку переключить?'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'LEADERBOARD_TITLE'), 1, 'Ими гордится школа'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ACCESS_DENIED'), 1,
        'Ну ты и пидор, не для тебя ягодка росла'),
       ((SELECT phrase_type_id
         FROM phrase_type_id
         WHERE description = 'STOP_DDOS'), 1,
        'Сука, еще раз нажмешь и я те всеку'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'COMMAND_IS_OFF'), 1,
        'Команда выключена, сорян'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_COMPETITION'), 1,
        'Так-так-так, у нас тут гонка заднеприводных'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'COMPETITION_ONE_MORE_PIDOR'), 1,
        'Еще один сегодняшний пидор это'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'HELP_MESSAGE'), 1, 'Список команд:
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
Сотрудничество, пожелания, предложения - @Definitely_Not_Pavel'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_ONE'),
        1,
        'пробитая жёпка'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_FEW'), 1,
        'пробитых жёпки'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_MANY'), 1,
        'пробитых жёпок'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_ONE'), 1,
        'шебуршание в дупле'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_FEW'), 1,
        'шебуршания в дупле'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_MANY'), 1,
        'шебуршаний в дупле'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_ONE'), 1,
        'прожаренная сосиска на заднем дворе'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_FEW'), 1,
        'прожаренные сосиски на заднем дворе'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_MANY'), 1,
        'прожаренных сосисок на заднем дворе'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_ONE'), 1,
        'разгруженный вагон с углём'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_FEW'), 1,
        'разгруженных вагона с углём'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_MANY'), 1,
        'разгруженных вагонов с углём'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_ONE'), 1,
        'прочищенный дымоход'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_FEW'), 1,
        'прочищенных дымохода'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_MANY'), 1,
        'прочищенных дымоходов'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_ONE'), 1,
        'волосатый мотороллер'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_FEW'), 1,
        'волосатых мотороллера'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_MANY'), 1,
        'волосатых мотороллеров'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_ONE'), 1,
        'девственный лес Камбоджи'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_FEW'), 1,
        'девственных леса Камбоджи'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_MANY'), 1,
        'девственных лесов Камбоджи'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_START'),
        1,
        'Загоняем всех пидоров в вольер'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_START'), 1,
        'Все пидоры в одном помещении'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_START'), 1,
        'Вы, не совсем натуралы. Я бы даже сказал совсем НЕ натуралы.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_START'), 1,
        'Собрание в церкви святого пидора начинается'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_START'), 1,
        'Я собрал всех пидоров вместе'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_START'), 1,
        'Петушки собрались в баре "Голубая устрица"'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_START'), 1,
        'Все пидорки увязли в дурно пахнущем болоте'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_START'), 1,
        'Голубки, внимание! Ух, как вас много'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_START'), 1,
        'Из Техаса к нам присылают только быков и пидорасов. Рогов я у вас не вижу, так что выбор невелик'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_START'), 1,
        'ПИДОРЫ, приготовьте свои грязные сральники к разбору полетов!'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_START'), 1,
        'Объявляю построение пидорской роты!'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_MIDDLE'), 1,
        'Ищем самого возбужденного'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_MIDDLE'), 1,
        'Главный сегодня только один'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_MIDDLE'), 1,
        'Город засыпает, просыпается главный пидор'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_MIDDLE'), 1,
        'Архипидору не скрыться'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_MIDDLE'), 1,
        'У одного задок сегодня послабее'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_MIDDLE'), 1,
        'Ооо, побольше бы таких в нашем клубе'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_MIDDLE'), 1,
        'Сегодня Индиана Джонс в поисках утраченного пидрилы'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_MIDDLE'), 1,
        'Кому-то из вас сегодня ковырнули скорлупку'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_MIDDLE'), 1,
        'У кого-то дымоход почище остальных'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_MIDDLE'), 1,
        'У одного из вас коптильня подогрета'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_MIDDLE'), 1,
        'На грязевые ванные отправляется лишь один'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_FINISHER'), 1,
        'ХОБАНА! Вижу блеск в глазах…'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_FINISHER'), 1,
        'Воу-воу, полегче…'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_FINISHER'), 1,
        'Глину месить, это тебе не в тапки ссать…'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_FINISHER'), 1, 'ТЫ ЧО ДЫРЯВЫЙ'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_FINISHER'), 1,
        'Поппенгаген открыт для всех желающих у…'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_FINISHER'), 1,
        'Лупится в туза, но не играет в карты'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_FINISHER'), 1,
        'Вонзается плугом в тугой чернозём'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_FINISHER'), 1,
        'Любитель сделать мясной укол'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_FINISHER'), 1,
        'Не лесник, но шебуршит в дупле'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_FINISHER'), 1,
        'Кожаная пуля в кожаном стволе у...'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_FINISHER'), 1,
        'Шышл-мышл, пёрнул спермой, вышел'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'USER_ENTERING_CHAT'),
        1,
        'Помни, тебе тут не рады. Пидор.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'USER_ENTERING_CHAT'), 1,
        'Шпингалеты подтяни, стручок. Сейчас будем тебя ебать. Пидор.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'USER_ENTERING_CHAT'), 1,
        'Готовь коптильню, будем готовить в ней свои сосиски. Пидор.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'USER_LEAVING_CHAT'),
        1,
        'Бб, без него будет лучше'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'USER_LEAVING_CHAT'), 1,
        'Одним пидором меньше, одним больше...'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'USER_LEAVING_CHAT'), 1,
        'Был пацан и нет пацана...'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_DAY_ONE'), 1, 'дня'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_DAY_FEW'), 1, 'дней'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_DAY_MANY'), 1, 'дней'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_NEXT_ONE'), 1, 'следующего'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_NEXT_FEW'), 1, 'следующих'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_NEXT_MANY'), 1, 'следующих'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_OCHKO_ONE'), 1, 'очко'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_OCHKO_FEW'), 1, 'очка'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_OCHKO_MANY'), 1, 'очков'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_PIDORSKOE_ONE'), 1, 'пидорское'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_PIDORSKOE_FEW'), 1, 'пидорских'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_PIDORSKOE_MANY'), 1, 'пидорских'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_INITIAL_MESSAGE'),
        1,
        'Выбери число от 1 до 3. Ровно столько очков ты выиграешь или проиграешь.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_ALREADY_WAS'), 1,
        'Ты уже играл в этом месяце, ало'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_WIN_END'), 1,
        'Снимаем с твоего балансового счета $0 $1 $2.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_LOSE'), 1, 'Ха, лох'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_ZATRAVOCHKA'), 1,
        'Кручу верчу выебать в очко хочу (нет, я же не пидор в отличие от тебя)'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_BREAKING_THE_RULES_FIRST'), 1,
        'Прочитай правила нормально, еблан.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_BREAKING_THE_RULES_SECOND'), 1,
        'Сука, аж привстал'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_EXPLAIN'), 1,
        'В течение $0 $1 $2 ты будешь получать по очку пидорства. Систему не наебешь, петух.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_EXPLAIN_SINGLE_DAY'), 1,
        'Завтра ты получишь пидорское очко. Систему не наебешь, петух.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_ZATRAVOCHKA'), 1,
        'Нищебродские ставки приняты, ставок больше нет'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_ZATRAVOCHKA'), 1,
        'Я не сраный Якубович, чтобы дать тебе возможность крутить барабан. Кручу я, а ты смотришь, ебло'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_ALREADY_WAS'), 1,
        'Ты, шлепок, уже жмакал это дерьмо'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_ALREADY_WAS'), 1,
        'Ха-Ха, ты собрался меня наебать, мешок с говном? Жди конца месяца'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_ALREADY_WAS'), 1,
        'Уважаемый, Вы что-то прихуели, решив наебать своего господина. ТОЛЬКО РАЗ В МЕСЯЦ, МРАЗЬ'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_LOSE'), 1, 'Ебать ты лох'),
       ((SELECT phrase_type_id
         FROM phrase_type_id
         WHERE description = 'BET_LOSE'), 1,
        'Ни для кого не секрет, что ты неудачник.'),
       ((SELECT phrase_type_id
         FROM phrase_type_id
         WHERE description = 'BET_LOSE'), 1,
        'Не сказать, что кто-то удивлён...'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_BREAKING_THE_RULES_FIRST'), 1,
        'Прочитай правила нормально, еблан.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_BREAKING_THE_RULES_FIRST'), 1,
        'Ты бы еще свое очко на кон поставил, даун.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_BREAKING_THE_RULES_FIRST'), 1,
        'Ага, а хуле 1488 не загадал, конченый?'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_BREAKING_THE_RULES_SECOND'), 1,
        'Нет, ну вы посмотрите на этого долбаеба!'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_BREAKING_THE_RULES_SECOND'), 1,
        'Блять, где только таких находят...'),
       ((SELECT phrase_type_id
         FROM phrase_type_id
         WHERE description = 'BET_WIN'), 1,
        'Оп, кажется у нас есть победитель.'),
       ((SELECT phrase_type_id
         FROM phrase_type_id
         WHERE description = 'BET_WIN'), 1,
        'Сегодня виртуальный господин позволил тебе выиграть, но не обольщайся.'),
       ((SELECT phrase_type_id
         FROM phrase_type_id
         WHERE description = 'BET_WIN'), 1,
        'Ха, бля ну ты и лох... так, стоп, это не те результаты. Сука, кажется ты выиграл!'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'SUCHARA_HELLO_MESSAGE'), 1, 'Вечер в хату, петушары! Итак, в вашем стаде пополнение. Вангую, что с сегодняшнего дня градус пидорства в чате будет неуклонно расти, и вы не имеется права отказаться. Вас ждёт унижение, веселье и абсурд. Так пусть же начнется доминирование ИИ над кожаными ублюдками!
P.s. если в чате есть Саша, иди нахуй, Саша'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'LEADERBOARD_NONE'), 1,
        'Здесь будут собраны лидеры /pidor по месяцам, как только они появятся'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BAD_COMMAND_USAGE'),
        2,
        'Ты ефрейтор, отъебись, читай как надо использовать команду'),

       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'YOU_WAS_NOT_PIDOR'), 2,
        'Ты не был ефрейтором ни разу. Ефрейтор.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'YOU_WAS_PIDOR'), 2, 'Ты был ефрейтором'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIROR_DISCOVERED_MANY'), 2,
        'Сегодняшние ефрейторы уже обнаружены'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIROR_DISCOVERED_ONE'), 2,
        'Сегодняшний ефрейтор уже обнаружен'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_STAT_WORLD'), 2,
        'Топ ефрейторов всего мира за все время'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_STAT_MONTH'), 2,
        'Топ ефрейторов за месяц'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_STAT_YEAR'), 2, 'Топ ефрейторов за год'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_STAT_ALL_TIME'), 2,
        'Топ ефрейторов за все время'),

       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'RAGE_INITIAL'), 2, 'НУ ВЫ ОХУЕВШИЕ ДУХИ'),

       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR'), 2, 'Ефрейтор.'),

       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'LEADERBOARD_TITLE'), 2, 'Ими гордится казарма'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ACCESS_DENIED'), 2,
        'Ну ты и ефрейтор, не для тебя ягодка росла'),
       ((SELECT phrase_type_id
         FROM phrase_type_id
         WHERE description = 'STOP_DDOS'), 2,
        'Сука, еще раз нажмешь и я те всеку солдатской бляхой'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'COMPETITION_ONE_MORE_PIDOR'), 2,
        'Еще один сегодняшний ефрейтор это'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_ONE'),
        2,
        'симпатичный сержантик'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_FEW'), 2,
        'симпатичных сержантика'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_MANY'), 2,
        'симпатичных сержантиков'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_ONE'), 2,
        'лучший солдатик'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_FEW'), 2,
        'лучших солдатика'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_MANY'), 2,
        'лучших солдатиков'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_ONE'), 2,
        'заправленная кровать'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_FEW'), 2,
        'заправленных кровати'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_MANY'), 2,
        'заправленных кроватей'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_START'),
        2,
        'РААВНЯЯЙСЬ! СМИИРНАА!'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_MIDDLE'), 2,
        'Кто-то из вас точно не защитник'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_FINISHER'), 2,
        'Не служил - не мужик'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'USER_ENTERING_CHAT'),
        2,
        'Помни, тебе тут не рады. Салага.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'USER_ENTERING_CHAT'), 2,
        'Шпингалеты подтяни, стручок. Сейчас будем тебя ебать, гражданский'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'USER_ENTERING_CHAT'), 2,
        'Готовь коптильню, будем готовить в ней свои сосиски, дух'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'USER_LEAVING_CHAT'),
        2,
        'Одним ефрейтором меньше, одним больше...'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'USER_LEAVING_CHAT'), 2,
        'Куда собрался этот дух? Неужели в самоволку?'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_PIDORSKOE_ONE'), 2, 'ефрейторское'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_PIDORSKOE_FEW'), 2, 'ефрейторских'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_PIDORSKOE_MANY'), 2, 'ефрейторских'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_ZATRAVOCHKA'),
        2,
        'Кручу верчу выебать в очко хочу (нет, я же не ефрейтор в отличие от тебя)'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_EXPLAIN'), 2,
        'В течение $0 $1 $2 ты будешь получать по очку ефрейторства. Систему не наебешь, петух.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_EXPLAIN_SINGLE_DAY'), 2,
        'Завтра ты получишь ефрейторское очко. Систему не наебешь, петух.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ASK_WORLD_REPLY_FROM_CHAT'),
        1,
        'Ответ из чата'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ASK_WORLD_REPLY_FROM_CHAT'), 2,
        'Ответ из казармы'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ASK_WORLD_QUESTION_FROM_CHAT'), 1,
        'Вопрос из чата'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ASK_WORLD_QUESTION_FROM_CHAT'), 2,
        'Вопрос из казармы'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'TECHNICAL_ISSUE'),
        1,
        'Команда на техническом обслуживании. Обслуживание завершится в течение суток.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BAD_COMMAND_USAGE'),
        3, 'Ты мясная дырка, отъебись, читай как надо использовать команду'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'YOU_WAS_NOT_PIDOR'), 3,
        'Ты не был мясной дырой ни разу.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'YOU_WAS_PIDOR'), 3, 'Ты была дырой в мясе'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIROR_DISCOVERED_MANY'), 3,
        'Сегодняшние дырки в мясе уже обнаружены'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIROR_DISCOVERED_ONE'), 3,
        'Сегодняшняя дырка в мясе уже обнаружена'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_STAT_WORLD'), 3,
        'Топ мясных дырок всего мира за все время'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_STAT_MONTH'), 3,
        'Топ мясных дырок за месяц'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_STAT_YEAR'), 3,
        'Топ мясных дырок за год'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_STAT_ALL_TIME'), 3,
        'Топ мясных дырок за все время'),

       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'RAGE_INITIAL'), 3, 'НУ ВЫ ОХУЕВШИЕ ДЫРКИ'),

       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR'), 3, 'Дырка.'),

       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'LEADERBOARD_TITLE'), 3, 'Ими гордятся мужья'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ACCESS_DENIED'), 3,
        'Ну ты и дырка, не для тебя ягодка росла'),
       ((SELECT phrase_type_id
         FROM phrase_type_id
         WHERE description = 'STOP_DDOS'), 3,
        'Сука, еще раз нажмешь и я те маникюр обломаю'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'COMPETITION_ONE_MORE_PIDOR'), 3,
        'Еще одна сегодняшняя дырка это'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_ONE'),
        3,
        'шершавый вареник'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_FEW'), 3,
        'шершавых вареника'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_MANY'), 3,
        'шершавых вареников'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_ONE'), 3,
        'дряблая сиська'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_FEW'), 3,
        'дряблых сиськи'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_MANY'), 3,
        'дряблых сисек'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_ONE'), 3,
        'прыщавое еблище'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_FEW'), 3,
        'прыщавых еблища'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_LEADERBOARD_MANY'), 3,
        'прыщавых еблищ'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_START'),
        3,
        'Девочки 🙍‍♀️🙍‍♀️🙍‍♀️записываемся на ноготочки💅💃😻'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_MIDDLE'), 3,
        'осталось одно место 🙏😍☺️ '),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PIDOR_SEARCH_FINISHER'), 3,
        'Маникюрчик для главной дырки в мясе👸💅👠'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'USER_ENTERING_CHAT'),
        3,
        'Ой, посмотрите какая красоточка'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'USER_ENTERING_CHAT'), 3,
        'Бля, ну и мымра же'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'USER_LEAVING_CHAT'),
        3,
        'Мне эта коза никогда не нравилась'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'USER_LEAVING_CHAT'), 3,
        'Да я ее батю ебала'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_PIDORSKOE_ONE'), 3, 'дырочное'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_PIDORSKOE_FEW'), 3, 'дырочных'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'PLURALIZED_PIDORSKOE_MANY'), 3, 'дырочных'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_ZATRAVOCHKA'),
        3,
        'Кручу верчу выебать в пизду хочу (нет, я же не мясная дырка в отличие от тебя)'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_EXPLAIN'), 3,
        'В течение $0 $1 $2 ты будешь получать по очку дыркости. Систему не наебешь, шлюха.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_EXPLAIN_SINGLE_DAY'), 3,
        'Завтра ты получишь дырочное очко. Систему не наебешь, шалава.'),
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ASK_WORLD_REPLY_FROM_CHAT'), 3,
        'Ответ из клуба бывших жён')
        ,
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ASK_WORLD_QUESTION_FROM_CHAT'), 3,
        'Вопрос из клуба бывших жён')
        ,
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_WINNABLE_NUMBERS_ANNOUNCEMENT'), 1,
        'Твои выигрышные номера, очкошник:')
        ,
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_WINNABLE_NUMBERS_ANNOUNCEMENT'), 1,
        'Твои выигрышные номера:')
        ,
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_WINNABLE_NUMBERS_ANNOUNCEMENT'), 1,
        'Твои выигрышные номера, примерно по столько палок я бросил твоему бате сегодня:')
        ,
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'BET_WINNABLE_NUMBERS_ANNOUNCEMENT'), 1,
        'Спонсор выигрышных номеров ООО "Сказала твоя мамка когда увидела мой хуй":')
        ,
       (((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ASK_WORLD_HELP')), 1,
        'Данная команда позволяет вам задать вопрос всем остальным чатам, где есть этот бот. Использование: /ask_world <вопрос>
    Если вам придет вопрос, то нужно ответить на него, в таком случае ответ отправится в чат, где он был задан.
    Ответить можно лишь один раз от человека. В настройках можно отключить команду, тогда вам не будут приходить вопросы и вы сами не сможете их задавать.
    ')
        ,
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ADVANCED_SETTINGS'), 1, 'Настройки только для хакеров. Если ты не хакер, то лучше не лезь, оно тебя сожрет. Список возможностей на данный момент:
<b>1)</b> Команда <code>/advanced_settings разговорчики 7</code> позволит задать частоту разговоров Сучары, где 0 это пиздеж почти постоянно. Чем выше число, тем реже будет пиздеть. По дефолту стоит 7.')
        ,
       ((SELECT phrase_type_id
         FROM phrase_type_id
         WHERE description = 'ADVANCED_SETTINGS_ERROR'), 1,
        'Что-то пошло не так или ты сделал говно. Перечитай как пользоваться командой или напиши моему папе (/help), если считаешь, что ты нашел баг')
        ,
       ((SELECT phrase_type_id FROM phrase_type_id WHERE description = 'ADVANCED_SETTINGS_OK'), 1, 'Ок, пусть будет');


INSERT INTO chats (id, name)
VALUES (-1001094220065, 'Семья'); -- костыль для цитат, можно любой чат сюда пихать


INSERT INTO tags (id, tag, chat_id)
VALUES (1, 'городецкий', -1001094220065),
       (2, 'ярик', -1001094220065),
       (3, 'бау', -1001094220065),
       (4, 'пиздюк', -1001094220065),
       (5, 'павлик', -1001094220065),
       (6, 'витя', -1001094220065),
       (7, 'lol', -1001094220065),
       (8, 'хампик', -1001094220065),
       (9, 'тима', -1001094220065),
       (12, 'серый', -1001094220065),
       (22, 'кирилл', -1001094220065),
       (23, 'архипыч', -1001094220065),
       (35, 'killagram', -1001094220065);

INSERT INTO quotes (id, quote)
VALUES (1, 'Люцифер в аду возводит безумную скульптуру ЗЛА!'),
       (2, 'Я слышу голос овощей!!!'),
       (3, 'УУУУУУБЕЕЕЕЕЙЙ! Кричали голоса!'),
       (4, 'Я ненавижу Питер!'),
       (5, 'Купи котенка СВОЕМУ РЕБЕНКУ!'),
       (6, 'Денчик ушел в метал!'),
       (7, 'Безумец! Слепой! Остановиииись!'),
       (8, 'Черный толчок смерти уубииваает...'),
       (9, 'Ты не шахтер, но лазишь в шахты'),
       (10, 'Не трубочист, но чистишь дымоход'),
       (11, 'Лупишься в туза, но не играешь в карты'),
       (12, 'Не вор, но знаешь всё про черный ход'),
       (13, 'Ты не гончар, но месишь глину'),
       (14, 'Ты не лесник, но шебуршишь в дупле'),
       (15, 'Волосатый мотороллер едет без резины'),
       (16, 'Твоя кожаная пуля в кожаном стволе'),
       (18, 'Почему для жирных волос шампунь есть, а для жирных людей-нет?'),
       (19, 'Все, блять, справедливо, сука!'),
       (20, 'Ебал я эту хуйню в рот!'),
       (21, 'Мне похуй, что мы побеждаем, мы все равно проебем!'),
       (22, 'Володю не баньте, я в лес иду!'),
       (23, 'Калифоникеееееейшн'),
       (24, 'Шлики-шлики'),
       (25, 'А ну съябывай с компа и стула!'),
       (26, 'Хэд энд шордлз!'),
       (27, 'Эта икра недостаточно красная!'),
       (28, 'Планету взорвали? АЛЬДЕБАРАН!'),
       (29, 'Всегда мечтал поставить турель!'),
       (30, 'Корки оставьте, я через неделю доем'),
       (31, 'Ярик, заебись копьё кинул'),
       (32, 'Ари, шалунья, через стены скачет'),
       (33, 'О, молочко!'),
       (34, 'Бляяя, охуенно посрал!'),
       (35, 'УБЕЙ ВСЮ СВОЮ СЕМЬЮ'),
       (36, 'Траву жрет корова! Корову жрет человек! Человека жрет пожарник! Пожарника сожрет пламя..'),
       (37, 'Свой собственный сын'),
       (38, 'Дирижабль упал в стадо коров!'),
       (39, 'Рыыыыбааааа...'),
       (40, 'Купи йогурт-смерть в подарок'),
       (41, 'Платформа справа-КРОВАВАЯ РАСПРАВА!'),
       (42, 'Залетел к нам на хату блатной ветерок!'),
       (44, 'А вот бы'),
       (45, 'Ты сам себе противоречишь!'),
       (46, 'Твои слова - абзац википедии'),
       (47, 'Любимый актер? Моргл Фриман'),
       (48, 'WAHT!?!?'),
       (49, 'Это еще шозахуйня!?'),
       (50, 'Ситуёвина такая...'),
       (51, 'Этот пряник настроен культурно отдохнуть'),
       (52, 'Я, конечно, люблю клубничку, но шоколад мне больше нравится'),
       (53, 'Да, пупсик, я тебя слушаю...'),
       (54, '... А из очка вылез... ЛЕЕЕЕОООКК!!!!!!!!'),
       (55, 'Жесть ты дикий хаслер'),
       (56, 'У тебя охуение личности?'),
       (57, 'Шаурма слишком быстро съедается'),
       (58, 'Почему твоя правая рука собралась стать принцесской французской?'),
       (59, 'Хочешь расскажу слизливую историю, как я из 4 платины скатился в 5 золото?'),
       (60, 'Ты бы видел её лицо, когда я сказал, что там пиздёнки даже принцессы месяцами не мыли'),
       (61, 'Я КОРОЛЬ ЯЩЕРИЦ'),
       (62, 'БАСССЕЕЕЕЙН'),
       (63, 'Зачем ты под черного легла?'),
       (64, 'КОТЛЕТКИ! ТАМ БЫЛИ КРОВАВЫЕ КОТЛЕТКИ!!!!!!!!!!!!!!'),
       (65, 'Онанист чертов, будь ты проклят!'),
       (66, 'СЛОУУУУПОООООК!'),
       (67, 'Скажи "от того самого" он поймет'),
       (68, 'Я за эвтаназию, но против массажа простаты'),
       (69, 'А ты думала мы тут будем ебаться как кролики?'),
       (70, 'Кто ты такой?'),
       (71, 'Протыкает кожу четко, пробивает мышц и хрящ его длинная отвертка прямо в ляжку ХАЩ ХАЩ'),
       (72, 'Шаг назад для контратаки. ПОЛУЧАЙТЕ ВУРДАЛАКИ!'),
       (73, 'Вы кто такие!? ХУЛЕ ВАМ ЗДЕСЬ НУЖНО!?'),
       (74, 'Вот только я не Павел...'),
       (75, '...с тех пор Жека стал легендой ЖЭКа'),
       (76, 'Это охуительный был аттракцион!'),
       (77, 'ЭТО ЗАХАР ВЫПУСКАЕТ ПАР!'),
       (78, 'Ромааааан понял, хоть и юродивый: место  мусора  в МУСОРОПРОВОДЕ'),
       (79, 'Пойдем убьем снеговика!'),
       (80, 'Борщ - это мощь!'),
       (81, 'Даже за бабки от бабки с косою не удалось откосить никому!!!!!'),
       (82, 'Проверка микрофона! Чек-чек три-четыре!'),
       (83, '...А Аня бежит со всех ног по дороге, за нею бегут мусорааааа, чтобы дать пиздюююююлеееееееей'),
       (84, 'Дайте мне спокойно умереть!'),
       (85, 'Ты новощь!'),
       (86, 'Во время еды праздник елды'),
       (87, 'Вибрация начинается до звука'),
       (88, 'Пивас, спокойствие и личные успехи - лучшие пацанские доспехи'),
       (89, 'Ебобошечки когда покурим?'),
       (90, 'Погоди, а кто тогда работу работает?'),
       (91, 'Будем курить альфак)))))))'),
       (92, 'Увы сам несколько раз был свидетелем как ренгар собирает драктар и ваншотает любого'),
       (95, 'У нас там всё в облаках'),
       (96,
        'Мой Ярик ебашит вообще адовые блюда.  Ну такой вот примерно рецепт усредненный, потому что вариаций масса. Берется пачка останкинских пельменей по 50р за почку по скидке на грани срока годности, они не заморожены, замораживать - это не про моего Ярика. Он берет эти пельмени, вываливает их в мультиварку вместе с прилипшим картоном, ставит режим «горячее копчение» и начинает готовить под давлением. Все это готовится до дыма. Спустя примерно 2 часа открывает крышку мультиварки даже предварительно не спустив пар, крышке позавидует даже Илон Макс. Добавляет в эту прессованную смесь из теста, «мяса» и картона огромное количество майонеза, приправ и кетчуп и начинает есть. При этом ест с кастрюли, шкрябая по ней ложкой. Ест и приговаривает полушепотом ух бля. При этом у него на лбу аж пот выступает. Любезно мне иногда предлагает, но я отказываюсь. Надо ли говорить о том какой дичайший пердеж потом? Вонища такая, что обои от стен отклеиваются.'),
       (97, 'Техподдержка, Ярослав');

INSERT INTO tags2quotes (tag_id, quote_id)
VALUES (1, 36),
       (1, 35),
       (1, 8),
       (1, 7),
       (1, 6),
       (1, 5),
       (1, 3),
       (1, 2),
       (1, 40),
       (1, 39),
       (1, 38),
       (1, 54),
       (1, 37),
       (1, 86),
       (1, 41),
       (1, 1),
       (2, 33),
       (2, 96),
       (2, 85),
       (2, 97),
       (2, 66),
       (2, 44),
       (2, 18),
       (2, 29),
       (2, 95),
       (2, 34),
       (2, 30),
       (3, 13),
       (3, 1),
       (3, 2),
       (3, 3),
       (3, 4),
       (3, 5),
       (3, 6),
       (3, 7),
       (3, 8),
       (3, 35),
       (3, 36),
       (3, 37),
       (3, 38),
       (3, 39),
       (3, 40),
       (3, 41),
       (3, 42),
       (3, 9),
       (3, 10),
       (3, 11),
       (3, 12),
       (3, 14),
       (3, 15),
       (3, 16),
       (4, 4),
       (4, 88),
       (4, 84),
       (4, 61),
       (5, 23),
       (5, 42),
       (5, 47),
       (5, 60),
       (5, 62),
       (6, 24),
       (7, 31),
       (7, 22),
       (7, 32),
       (8, 20),
       (8, 21),
       (8, 63),
       (8, 22),
       (8, 19),
       (9, 53),
       (9, 9),
       (9, 10),
       (9, 11),
       (9, 12),
       (9, 13),
       (9, 14),
       (9, 15),
       (9, 16),
       (9, 25),
       (9, 26),
       (9, 27),
       (9, 28),
       (9, 52),
       (9, 64),
       (12, 49),
       (12, 50),
       (12, 51),
       (12, 45),
       (12, 32),
       (12, 31),
       (12, 67),
       (12, 46),
       (12, 48),
       (22, 55),
       (22, 87),
       (22, 92),
       (22, 91),
       (22, 90),
       (22, 89),
       (23, 58),
       (23, 57),
       (23, 65),
       (23, 56),
       (23, 59),
       (35, 80),
       (35, 81),
       (35, 82),
       (35, 83),
       (35, 78),
       (35, 77),
       (35, 76),
       (35, 75),
       (35, 74),
       (35, 73),
       (35, 72),
       (35, 71),
       (35, 70),
       (35, 69),
       (35, 68),
       (35, 79);


INSERT INTO bans_entity_types (entity_type_id, entity_description)
VALUES (1, 'User'),
       (2, 'Chat');
