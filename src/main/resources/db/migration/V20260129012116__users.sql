create table public.users
(
    "id"             BIGSERIAL
        PRIMARY KEY,
    "names"          VARCHAR(90)  NOT NULL,
    "last_name"       VARCHAR(90)  NOT NULL,
    "second_last_name" VARCHAR(90),
    "email"          VARCHAR(120) NOT NULL,
    "password"       TEXT         NOT NULL,
    "phone"          VARCHAR(10),
    "user_type"       VARCHAR(50)  NOT NULL,
    "created_at"      TIMESTAMP    NOT NULL,
    "updated_at"      TIMESTAMP    NOT NULL,
    "deleted_at"      TIMESTAMP    NULL,
    CONSTRAINT unique_name_combination UNIQUE (names, "last_name", "second_last_name"),
    CONSTRAINT uk_users_email UNIQUE (email)
);

/*alter table public.users
    owner to postgres;*/