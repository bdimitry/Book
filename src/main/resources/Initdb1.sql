
    CREATE SCHEMA books;
    CREATE TABLE IF NOT EXISTS "books"."book"
    (
        name character varying(50) COLLATE pg_catalog."default",
        id SERIAL  NOT NULL,
        weight integer,
        age integer,
        CONSTRAINT "book_pkey" PRIMARY KEY (id)
    );

    CREATE TABLE IF NOT EXISTS "books"."image"
    (
        id SERIAL  NOT NULL,
        book_photo BYTEA,
        FOREIGN KEY (id)
        REFERENCES books.book (id)
        ON DELETE CASCADE
    );

    CREATE TABLE IF NOT EXISTS "books"."json_book"
    (
        id BIGSERIAL PRIMARY KEY,
        book JSONB,
        image_url VARCHAR(255)
    );