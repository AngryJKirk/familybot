CREATE TABLE IF NOT EXISTS chats (
  id   BIGINT PRIMARY KEY,
  name VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS users (
  id       BIGINT PRIMARY KEY,
  chat_id  BIGINT REFERENCES chats (id),
  name     VARCHAR(100),
  username VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS pidors (
  id         BIGINT REFERENCES users (id),
  pidor_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS quotes (
  id    SERIAL PRIMARY KEY,
  quote VARCHAR(10000) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tags (
  id  SERIAL PRIMARY KEY,
  tag VARCHAR(100) NOT NULL
);

CREATE UNIQUE INDEX title_idx
  ON tags (tag);

CREATE TABLE IF NOT EXISTS tags2quotes (
  tag_id   INT REFERENCES tags (id),
  quote_id INT REFERENCES quotes (id),
  PRIMARY KEY (tag_id, quote_id)
);


INSERT INTO tags (tag) VALUES
  ('Городецкий'),
  ('Ярик'),
  ('БАУ'),
  ('Пиздюк'),
  ('Павлик'),
  ('Витя'),
  ('LoL'),
  ('Хампик'),
  ('Тима');


INSERT INTO quotes (quote) VALUES
  ('Люцифер в аду возводит безумную ЗЛА!'),
  ('Я слышу голос овощей!!!'),
  ('УУУУУУБЕЕЕЕЕЙЙ! Кричали голоса!'),
  ('Я ненавижу Питер!'),
  ('Купи котенка СВОЕМУ РЕБЕНКУ!'),
  ('Денчик ушел в метал!'),
  ('Безумец! Слепой! Остановиииись!'),
  ('Черный толчок смерти уубииваает...'),
  ('Ты не шахтер, но лазишь в шахты '),
  ('Не трубочист, но чистишь дымоход ")'),
  ('Лупишься в туза, но не играешь в карты'),
  ('Не вор, но знаешь всё про черный ход'),
  ('Ты не гончар, но месишь глину "'),
  ('Ты не лесник, но шебуршишь в дупле'),
  ('Волосатый мотороллер едет без резины'),
  ('Твоя кожаная пуля в кожаном стволе'),
  ('Почему для жирных волос шампунь есть, а для жирных людей-нет? '),
  ('Все, блять, справедливо, сука!'),
  ('Ебал я эту хуйню в рот!'),
  ('Мне похуй, что мы побеждаем, мы все равно проебем! '),
  ('Володю не баньте, я в лес иду!'),
  ('Калифоникеееееейшн'),
  ('Шлики-шлики'),
  ('А ну съябывай с компа и стула!'),
  ('Хэд энд шордлз!'),
  ('Эта икра недостаточно красная!'),
  ('Планету взорвали? АЛЬДЕБАРАН!'),
  ('Всегда мечтал поставить турель!'),
  ('Корки оставьте, я через неделю доем'),
  ('Ярик, заебись копьё кинул'),
  ('Ари, шалунья, через стены скачет'),
  ('О, молочко!'),
  ('Бляяя, охуенно посрал!'),
  ('УБЕЙ ВСЮ СВОЮ СЕМЬЮ'),
  ('Траву жрет корова! Корову жрет человек! Человека жрет пожарник! Пожарника сожрет пламя..'),
  ('Свой собственный сын'),
  ('Дирижабль упал в стадо коров!'),
  ('Рыыыыбааааа...'),
  ('Купи йогурт-смерть в подарок'),
  ('Платформа справа-КРОВАВАЯ РАСПРАВА!'),
  ('Залетел к нам на хату блатной ветерок!');


INSERT INTO tags2quotes (tag_id, quote_id) VALUES
  (1, 1),
  (1, 2),
  (1, 3),
  (1, 5),
  (1, 6),
  (1, 7),
  (1, 8),
  (1, 35),
  (1, 36),
  (1, 37),
  (1, 38),
  (1, 39),
  (1, 40),
  (1, 41);

INSERT INTO tags2quotes (tag_id, quote_id) VALUES
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
  (3, 13),
  (3, 14),
  (3, 15),
  (3, 16);

INSERT INTO tags2quotes (tag_id, quote_id) VALUES
  (2, 18),
  (2, 29),
  (2, 33),
  (2, 34),
  (2, 30);

INSERT INTO tags2quotes (tag_id, quote_id) VALUES
  (4, 4);

INSERT INTO tags2quotes (tag_id, quote_id) VALUES
  (5, 23),
  (5, 42);

INSERT INTO tags2quotes (tag_id, quote_id) VALUES
  (6, 24);

INSERT INTO tags2quotes (tag_id, quote_id) VALUES
  (7, 22),
  (7, 31),
  (7, 32);

INSERT INTO tags2quotes (tag_id, quote_id) VALUES
  (8, 19),
  (8, 20),
  (8, 21),
  (8, 22);

INSERT INTO tags2quotes (tag_id, quote_id) VALUES
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
  (9, 28);
COMMIT;


CREATE TABLE IF NOT EXISTS commands (
  id      SERIAL PRIMARY KEY,
  command VARCHAR(50) UNIQUE
);

CREATE TABLE IF NOT EXISTS history (
  command_id BIGINT REFERENCES commands (id),
  user_id    BIGINT REFERENCES users (id),
  chat_id    BIGINT REFERENCES chats (id),
  command_date TIMESTAMP
);

INSERT INTO commands (command) VALUES
  ('/stats_month'),
  ('/stats_year'),
  ('/stats_total'),
  ('/pidor'),
  ('/quote');

CREATE TABLE IF NOT EXISTS chat_log (
  chat_id BIGINT REFERENCES chats(id),
  user_id BIGINT,
  message VARCHAR(10000)
)