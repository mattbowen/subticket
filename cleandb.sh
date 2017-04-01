#!/usr/bin/env bash
if [ -d "$SUBTICKET_DB_DIR" ]; then
	if pg_ctl status  -D "$SUBTICKET_DB_DIR"; then
		. stopdb.sh
	fi

	rm -rf "$SUBTICKET_DB_DIR"
else
	echo "$SUBTICKET_DB_DIR does not exist. Nothing to do"
fi
