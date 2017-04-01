CREATE TABLE users (
    username varchar(64),
    pw_hash char(60) NOT NULL, -- bcrypt
    PRIMARY KEY(username)
);
