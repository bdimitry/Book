SET search_path TO cats;

INSERT INTO cats.cat (name, age, weight)
VALUES ('Farcuad', 4, 4);

INSERT INTO cats.cat (name, age, weight)
VALUES ('John', 3, 6);

INSERT INTO cats.cat (name, age, weight)
VALUES ('Marvel', 5, 9);

INSERT INTO cats.cat (name, age, weight)
VALUES ('Funtick', 1, 2);

INSERT INTO cats.cat (name, age, weight)
VALUES ('Alex', 3, 3);

INSERT INTO cats.cat (name, age, weight)
VALUES ('Jerry', 8, 9);

INSERT INTO cats.cat (name, age, weight)
VALUES ('Tom', 2, 3);

INSERT INTO cats.cat (name, age, weight)
VALUES ('Aunkere', 4, 4);

INSERT INTO cats.cat (name, age, weight)
VALUES ('Zhlob', 5, 4);

INSERT INTO cats.cat (name, age, weight)
VALUES ('Andrey', 3, 4);

INSERT INTO cats.json_cat (cat)
VALUES ('{"name": "Farcuad", "age": 4, "weight": 4 }');

INSERT INTO cats.json_cat (cat)
VALUES ('{"name": "John", "age": 3, "weight": 6 }');

INSERT INTO cats.json_cat (cat)
VALUES ('{"name": "Marvel", "age": 5, "weight": 9 }');

INSERT INTO cats.json_cat (cat)
VALUES ('{"name": "Funtick", "age": 1, "weight": 2 }');

INSERT INTO cats.json_cat (cat)
VALUES ('{"name": "Alex", "age": 3, "weight": 3 }');

INSERT INTO cats.json_cat (cat)
VALUES ('{"name": "Tom", "age": 8, "weight": 9 }');

INSERT INTO cats.json_cat (cat)
VALUES ('{"name": "Jerry", "age": 2, "weight": 3 }');

INSERT INTO cats.json_cat (cat)
VALUES ('{"name": "Aunkere", "age": 4, "weight": 4 }');

INSERT INTO cats.json_cat (cat)
VALUES ('{"name": "Zhlob", "age": 5, "weight": 4 }');

INSERT INTO cats.json_cat (cat)
VALUES ('{"name": "Andrey", "age": 3, "weight": 4 }');