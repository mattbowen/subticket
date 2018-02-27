CREATE TABLE users (
    username varchar(64),
    pw_hash char(60) NOT NULL, -- bcrypt
    email varchar(128) NOT NULL,
    email_validated boolean NOT NULL DEFAULT false,
    created timestamp with time zone NOT NULL DEFAULT now(),
    PRIMARY KEY(username)
);
