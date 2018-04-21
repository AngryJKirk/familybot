CREATE TABLE IF NOT EXISTS chats (
  id     BIGINT PRIMARY KEY,
  name   VARCHAR(100),
  active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS users (
  id       BIGINT PRIMARY KEY,
  name     VARCHAR(100),
  username VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS pidors (
  id         BIGINT REFERENCES users (id),
  chat_id    BIGINT REFERENCES chats (id),
  pidor_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS quotes (
  id    SERIAL PRIMARY KEY,
  quote VARCHAR(10000) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tags (
  id      SERIAL PRIMARY KEY,
  tag     VARCHAR(100) NOT NULL UNIQUE,
  chat_id BIGINT REFERENCES chats (id)
);

CREATE TABLE IF NOT EXISTS tags2quotes (
  tag_id   INT REFERENCES tags (id),
  quote_id INT REFERENCES quotes (id),
  PRIMARY KEY (tag_id, quote_id)
);


CREATE TABLE IF NOT EXISTS commands (
  id      SERIAL PRIMARY KEY,
  command VARCHAR(50) UNIQUE
);

CREATE TABLE IF NOT EXISTS history (
  command_id   BIGINT REFERENCES commands (id),
  user_id      BIGINT REFERENCES users (id),
  chat_id      BIGINT REFERENCES chats (id),
  command_date TIMESTAMP
);

INSERT INTO commands (command) VALUES
  ('/stats_month'),
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
  ('/stats_world');


CREATE TABLE IF NOT EXISTS chat_log (
  chat_id BIGINT REFERENCES chats (id),
  user_id BIGINT REFERENCES users (id),
  message VARCHAR(10000)
);

CREATE TABLE IF NOT EXISTS pidor_dictionary_start (
  start VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS pidor_dictionary_middle (
  middle VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS pidor_dictionary_finisher (
  finisher VARCHAR(500)
);


CREATE TABLE users2chats (
  chat_id BIGINT REFERENCES chats (id),
  user_id BIGINT REFERENCES users (id),
  active  BOOLEAN default true,
  PRIMARY KEY (chat_id, user_id)
);


CREATE TABLE IF NOT EXISTS pidor_leaderboard_dictionary_plurs (
  id          INT PRIMARY KEY,
  description VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS pidor_leaderboard_dictionary_v2 (
  message VARCHAR(200) PRIMARY KEY,
  plur_id INT REFERENCES pidor_leaderboard_dictionary_plurs (id)
);

CREATE TABLE IF NOT EXISTS functions (
  function_id INTEGER PRIMARY KEY,
  description VARCHAR(200)
);

INSERT INTO functions (function_id, description) VALUES
  (1, 'Хуификация'),
  (2, 'Общение'),
  (3, 'Пидор дня'),
  (4, 'Рейдж'),
  (5, 'АнтиДДос'),
  (6, 'Вопросы миру');

CREATE TABLE IF NOT EXISTS function_settings (
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
  add column file_id varchar(500) default null 

