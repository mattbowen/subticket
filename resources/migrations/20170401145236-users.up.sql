CREATE TABLE users (
    username varchar(64),
    pw_hash char(60) NOT NULL, -- bcrypt
    email varchar(128) NOT NULL,
    email_validated boolean NOT NULL DEFAULT false,
    created timestamp with time zone NOT NULL DEFAULT now(),
    PRIMARY KEY (username)
);

CREATE TABLE namespaces (
    name varchar(64),
    write varchar(64) NOT NULL,
    PRIMARY KEY (name),
    FOREIGN KEY (name, write) REFERENCES sets (namespace, name) -- TODO on delete.
);

CREATE TYPE set_type as ENUM ('concrete', 'expression');

CREATE TABLE sets (
    namespace varchar(64),
    name varchar(64),
    type set_type NOT NULL,
    write varchar(64) NOT NULL,
    PRIMARY KEY (namespace, name),
    FOREIGN KEY (namespace, write) REFERENCES sets (namespace, name) -- TODO on delete
);

CREATE TABLE concrete_sets (
    namespace varchar(64),
    name varchar(64),
    username varchar(64),
    PRIMARY KEY (namespace, name, username),
    FOREIGN KEY (namespace, name) REFERENCES sets (namespace, name), -- TODO check type.
    FOREIGN KEY (username) REFERENCES users (username) -- TODO on delete
);

CREATE TABLE expression_sets (
    namespace varchar(64),
    name varchar(64),
    literal jsonb,
    PRIMARY KEY (namespace, name),
    FOREIGN KEY (namespace, name) REFERENCES sets (namespace, name), -- TODO check type.
);

CREATE TABLE child_sets (
    namespace varchar(64),
    parent varchar(64),
    child varchar(64),
    PRIMARY KEY (namespace, parent, child),
    FOREIGN KEY (namespace, parent) REFERENCES expression_sets (namespace, name),
    FOREIGN KEY (namespace, child) REFERENCES sets (namespace, name)
);

CREATE TABLE anf_terms (
    namespace varchar(64),
    name varchar(64),
    term integer,
    set varchar(64),
    PRIMARY KEY (namespace, name, term, set),
    FOREIGN KEY (namespace, name) REFERENCES expression_sets (namespace, name),
    FOREIGN KEY (namespace, set) REFERENCES sets (namespace, name)
);
