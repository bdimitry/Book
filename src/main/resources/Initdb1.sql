CREATE SCHEMA books;

CREATE SCHEMA acl;

------------------------------------------

CREATE TABLE IF NOT EXISTS "acl"."acl_sid" (
    id bigserial not null primary key,
    principal boolean not null,
    sid varchar(100) not null,
    constraint unique_uk_1 unique(sid, principal)
);

CREATE TABLE IF NOT EXISTS "acl"."acl_class" (
    id bigserial not null primary key,
    class varchar(100) not null,
    constraint unique_uk_2 unique(class)
);

CREATE TABLE IF NOT EXISTS "acl"."acl_object_identity" (
    id bigserial primary key,
    object_id_class bigint not null,
    object_id_identity varchar(36) not null,
    parent_object bigint,
    owner_sid bigint,
    entries_inheriting boolean not null,
    constraint unique_uk_3 unique(object_id_class, object_id_identity),
    constraint foreign_fk_2 foreign key(object_id_class) references acl.acl_class(id),
    constraint foreign_fk_3 foreign key(owner_sid) references acl.acl_sid(id)
);

ALTER TABLE "acl"."acl_object_identity"
ADD CONSTRAINT foreign_fk_1
FOREIGN KEY (parent_object)
REFERENCES "acl"."acl_object_identity" (id);

CREATE TABLE IF NOT EXISTS "acl"."acl_entry" (
    id bigserial primary key,
    acl_object_identity bigint not null,
    ace_order int not null,
    sid bigint not null,
    mask integer not null,
    granting boolean not null,
    audit_success boolean not null,
    audit_failure boolean not null,
    constraint unique_uk_4 unique(acl_object_identity, ace_order),
    constraint foreign_fk_4 foreign key(acl_object_identity) references acl.acl_object_identity(id),
    constraint foreign_fk_5 foreign key(sid) references acl.acl_sid(id)
);

------------------------------------------

CREATE TABLE IF NOT EXISTS "books"."users" (
    id SERIAL NOT NULL,
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS "books"."authorities" (
    username varchar(50) NOT NULL,
    authority varchar(50) NOT NULL,
    CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES "books"."users" (username)
);

CREATE UNIQUE INDEX ix_auth_username ON "books"."authorities" (username, authority);


------------------------------------------

CREATE TABLE IF NOT EXISTS "books"."book" (
    name character varying(50) COLLATE pg_catalog."default",
    id SERIAL NOT NULL,
    lastReaded integer,
    author character varying(50),
    CONSTRAINT "book_pkey" PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS "books"."image" (
    id SERIAL NOT NULL,
    book_photo BYTEA,
    FOREIGN KEY (id)
    REFERENCES books.book (id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "books"."json_book" (
    id BIGSERIAL PRIMARY KEY,
    book JSONB,
    image_url VARCHAR(255)
);

------------------------------------------