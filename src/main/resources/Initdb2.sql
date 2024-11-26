SET search_path TO books;

INSERT INTO books.book (name, author, lastReaded)
VALUES ('Farcuad', 'Jakob', 4);



INSERT INTO books.book (name, author, lastReaded)
VALUES ('Marvel', 'Grace', 9);

INSERT INTO books.book (name, author, lastReaded)
VALUES ('Funtick', 'Jakob', 2);

INSERT INTO books.book (name, author, lastReaded)
VALUES ('Alex', 'bolton', 3);

INSERT INTO books.book (name, author, lastReaded)
VALUES ('Jerry', 'bolton', 9);

INSERT INTO books.book (name, author, lastReaded)
VALUES ('Tom', 'Henry', 3);

INSERT INTO books.book (name, author, lastReaded)
VALUES ('Aunkere', 'Farcuad', 4);

INSERT INTO books.book (name, author, lastReaded)
VALUES ('Zhlob', 'Henry', 4);

INSERT INTO books.book (name, author, lastReaded)
VALUES ('Andrey', 'bolton', 4);

INSERT INTO books.json_book (book)
VALUES ('{"name": "Farcuad", "author": "Henry", "lastReaded": 4 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "John", "author": "Henry", "lastReaded": 6 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "Marvel", "author": "bolton", "lastReaded": 9 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "Funtick", "author": "bolton", "lastReaded": 2 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "Alex", "author": "Farcuad", "lastReaded": 3 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "Tom", "author": "Farcuad", "lastReaded": 9 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "Jerry", "author": "Jakob", "lastReaded": 3 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "Aunkere", "author": "Jakob", "lastReaded": 4 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "Zhlob", "author": "Grace", "lastReaded": 4 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "Andrey", "author": "Grace", "lastReaded": 4 }');