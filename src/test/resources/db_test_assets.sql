insert into chats (id, name)
VALUES (10, 'Test chat #10') ,
    (20, 'Test chat #20')
    ,
    (30, 'Test chat #30');

insert into users (id, name, username)
VALUES (1, 'Test user #1', 'user1') ,
    (2, 'Test user #2', 'user2')
    ,
    (3, 'Test user #3', 'user3');

insert into users2chats (chat_id, user_id)
VALUES (10, 1) ,
    (10, 2)
    ,
    (10, 3)
    ,
    (20, 1)
    ,
    (20, 2)
    ,
    (20, 3)
    ,
    (30, 1)
    ,
    (30, 2)
    ,
    (30, 3);


INSERT into chat_log (chat_id, user_id, message)
VALUES (10, 1, random()::text) ,
    (10, 2, random():: text)
    ,
    (10, 3, random():: text)
    ,
    (20, 1, random():: text)
    ,
    (20, 2, random():: text)
    ,
    (20, 3, random():: text)
    ,
    (30, 1, random():: text)
    ,
    (30, 2, random():: text)
    ,
    (30, 3, random():: text)
    ,
    (10, 1, random():: text)
    ,
    (10, 2, random():: text)
    ,
    (10, 3, random():: text)
    ,
    (20, 1, random():: text)
    ,
    (20, 2, random():: text)
    ,
    (20, 3, random():: text)
    ,
    (30, 1, random():: text)
    ,
    (30, 2, random():: text)
    ,
    (30, 3, random():: text)
    ,
    (10, 1, random():: text)
    ,
    (10, 2, random():: text)
    ,
    (10, 3, random():: text)
    ,
    (20, 1, random():: text)
    ,
    (20, 2, random():: text)
    ,
    (20, 3, random():: text)
    ,
    (30, 1, random():: text)
    ,
    (30, 2, random():: text)
    ,
    (30, 3, random():: text);
