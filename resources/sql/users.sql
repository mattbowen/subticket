-- name: add-user!
-- Inserts a user.
INSERT INTO users VALUES (:username, :pw_hash)
ON CONFLICT DO NOTHING;

-- name: get-hash
-- get's the hashed password for a user.
SELECT pw_hash from users where username = :username;
