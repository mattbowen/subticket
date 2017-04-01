#!/usr/bin/env bash

echo "Setting up database $SUBTICKET_DB for user $SUBTICKET_DB_USER"
createdb -h "$SUBTICKET_DB_HOSTNAME" -p "$SUBTICKET_DB_PORT" "$SUBTICKET_DB"
psql -h "$SUBTICKET_DB_HOSTNAME" -p "$SUBTICKET_DB_PORT" "$SUBTICKET_DB" <<EOF
create user "$SUBTICKET_DB_USER" password '$SUBTICKET_DB_PASS';
create schema "$SUBTICKET_DB_SCHEMA" authorization "$SUBTICKET_DB_USER";
grant all privileges on schema "$SUBTICKET_DB_SCHEMA" to "$SUBTICKET_DB_USER";
alter user "$SUBTICKET_DB_USER" set search_path to "$SUBTICKET_DB_SCHEMA";
EOF
