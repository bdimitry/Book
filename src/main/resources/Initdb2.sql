SET search_path TO cats;

INSERT INTO cats.cat (name, age, weight)
VALUES ('Tom', 3, 4);

INSERT INTO cats.cat (name, age, weight)
VALUES ('Jerry', 4, 5);

INSERT INTO cats.json_cat (cat)
VALUES ('{"name": "Farcuad", "age": 4, "weight": 4 }');

INSERT INTO cats.json_cat (cat)
VALUES ('{"name": "John", "age": 3, "weight": 3 }');