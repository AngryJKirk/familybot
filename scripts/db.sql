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
    id      BIGSERIAL NOT NULL,
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

INSERT INTO phrase_theme (description, active_by_default)
VALUES ('DEFAULT', TRUE),
       ('DAY_OF_DEFENDER_23_FEB', FALSE),
       ('DAY_OF_WOMAN_8_MARCH', FALSE),
       ('UKRAINIAN', FALSE);


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
