SET search_path TO books;

INSERT INTO books.book (name, author, weight)
VALUES ('Farcuad', 'Jakob', 4);



INSERT INTO books.book (name, author, weight)
VALUES ('Marvel', 'Grace', 9);

INSERT INTO books.book (name, author, weight)
VALUES ('Funtick', 'Jakob', 2);

INSERT INTO books.book (name, author, weight)
VALUES ('Alex', 'bolton', 3);

INSERT INTO books.book (name, author, weight)
VALUES ('Jerry', 'bolton', 9);

INSERT INTO books.book (name, author, weight)
VALUES ('Tom', 'Henry', 3);

INSERT INTO books.book (name, author, weight)
VALUES ('Aunkere', 'Farcuad', 4);

INSERT INTO books.book (name, author, weight)
VALUES ('Zhlob', 'Henry', 4);

INSERT INTO books.book (name, author, weight)
VALUES ('Andrey', 'bolton', 4);

INSERT INTO books.json_book (book)
VALUES ('{"name": "Farcuad", "age": 4, "weight": 4 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "John", "age": 3, "weight": 6 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "Marvel", "age": 5, "weight": 9 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "Funtick", "age": 1, "weight": 2 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "Alex", "age": 3, "weight": 3 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "Tom", "age": 8, "weight": 9 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "Jerry", "age": 2, "weight": 3 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "Aunkere", "age": 4, "weight": 4 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "Zhlob", "age": 5, "weight": 4 }');

INSERT INTO books.json_book (book)
VALUES ('{"name": "Andrey", "age": 3, "weight": 4 }');