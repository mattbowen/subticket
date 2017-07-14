(ns subticket.users
  (:require [subticket dbcon 
             [util :refer [client-error]]]
            [yesql.core :as yesql])
  (:import org.mindrot.jbcrypt.BCrypt))

(yesql/defqueries "sql/users.sql" subticket.dbcon/db)

(defn- add-pw-hash
  [request]
  (assoc request :pw_hash (BCrypt/hashpw (:password request) (BCrypt/gensalt))))

(defn add-user-handler
  [request]
  (when (= 0 (add-user! (add-pw-hash request)))
    (client-error "User exists")))


