    -- Table: Cats.Cats

    -- DROP TABLE IF EXISTS "cats"."cat"

    CREATE TABLE IF NOT EXISTS "cats"."cat"
    (
        name character varying(50) COLLATE pg_catalog."default",
        id SERIAL  NOT NULL,
        weight integer,
        age integer,
        CONSTRAINT "cat_pkey" PRIMARY KEY (id)
        cat_photo BYTEA
    )


    TABLESPACE pg_default;

    ALTER TABLE IF EXISTS "cats"."cat"
        OWNER to postgres;


        CREATE TABLE IF NOT EXISTS "cats"."image"
        (
            id SERIAL  NOT NULL,
            cat_photo BYTEA,
            FOREIGN KEY (id)
            REFERENCES cat (id)
            ON DELETE CASCADE
        )
    TABLESPACE pg_default;

    ALTER TABLE IF EXISTS "cats"."image"
        OWNER to postgres;