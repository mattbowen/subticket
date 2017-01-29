#!/usr/bin/env bash

# initdb dir
# postgres -D dir

USER=patrick2
PASS=5555
DB=subticket
SCHEMA=subticket

createdb "$DB"
psql "$DB" <<EOF
create user "$USER" password '$PASS';
create schema "$SCHEMA" authorization "$USER";
grant all privileges on schema "$SCHEMA" to "$USER";
alter user "$USER" set search_path to "$SCHEMA";
EOF
