#!/usr/bin/env bash

. dev_environment

new_db=false

if [ "$SUBTICKET_DB_HOSTNAME" = "localhost" -a -n $SUBTICKET_DB_DIR ]; then
	if [ -d "$SUBTICKET_DB_DIR" ]; then
		echo "Database directory $SUBTICKET_DB_DIR found."
	else
		mkdir -p "$SUBTICKET_DB_DIR"
		pg_ctl init -D "$SUBTICKET_DB_DIR"
		echo "Database directory $SUBTICKET_DB_DIR created. Delete it to start fresh."
		new_db=true
	fi
	
	if pg_ctl status  -D "$SUBTICKET_DB_DIR"; then
		echo "Postgres is running! Won't attempt to start it."
	else
		pg_ctl start -D "$SUBTICKET_DB_DIR" -o "-p $SUBTICKET_DB_PORT -k ''" -w # Note this only allows
		                                                              # connecting over TCP
		echo "Postgres started on port $SUBTICKET_DB_PORT"
	fi
	
	if $new_db; then
	. initdb.sh
	fi
else
	echo "Assuming Postgres is setup correctly on Host: $SUBTICKET_DB_HOSTNAME Port: $SUBTICKET_DB_PORT"
fi
lein migratus migrate
lein run
