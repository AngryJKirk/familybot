insert into chats (id, name)
VALUES (1, 'Test chat #1'),
       (2, 'Test chat #2'),
       (3, 'Test chat #3');

insert into users (id, name, username)
VALUES (1, 'Test user #1', '@user1'),
       (2, 'Test user #2', '@user2'),
       (3, 'Test user #3', '@user3');

insert into users2chats (chat_id, user_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (2, 1),
       (2, 2),
       (2, 3),
       (3, 1),
       (3, 2),
       (3, 3);


INSERT into chat_log (chat_id, user_id, message)
VALUES (1, 1, random()::text),
       (1, 2, random()::text),
       (1, 3, random()::text),
       (2, 1, random()::text),
       (2, 2, random()::text),
       (2, 3, random()::text),
       (3, 1, random()::text),
       (3, 2, random()::text),
       (3, 3, random()::text),
       (1, 1, random()::text),
       (1, 2, random()::text),
       (1, 3, random()::text),
       (2, 1, random()::text),
       (2, 2, random()::text),
       (2, 3, random()::text),
       (3, 1, random()::text),
       (3, 2, random()::text),
       (3, 3, random()::text),
       (1, 1, random()::text),
       (1, 2, random()::text),
       (1, 3, random()::text),
       (2, 1, random()::text),
       (2, 2, random()::text),
       (2, 3, random()::text),
       (3, 1, random()::text),
       (3, 2, random()::text),
       (3, 3, random()::text);
