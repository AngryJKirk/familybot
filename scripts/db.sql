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
       (23, '/advanced_settings'),
       (24, '/stats_strikes')
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

INSERT INTO bans_entity_types (entity_type_id, entity_description)
VALUES (1, 'User'),
       (2, 'Chat');
