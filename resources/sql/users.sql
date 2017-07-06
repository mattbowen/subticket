-- name: add-user!
-- Inserts a user.
INSERT INTO users VALUES (:username, :pw_hash)
ON CONFLICT DO NOTHING;
