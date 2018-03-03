(ns subticket.users
  (:require [subticket dbcon
             [util :refer [client-error]]]
            [yesql.core :as yesql]
            [io.pedestal.log :as log])
  (:import org.mindrot.jbcrypt.BCrypt))

(yesql/defqueries "sql/users.sql" subticket.dbcon/db)

(defn- add-pw-hash
  [request]
  (assoc request :pw_hash (BCrypt/hashpw (:password request) (BCrypt/gensalt))))

(defn add-user-handler
  [request]
  (when (= 0 (add-user! (add-pw-hash request)))
    (client-error "User exists")))

(defn login
  [{:keys [username password] :as request}]
  (log/trace :msg username)
  (if-let [hash (:pw_hash (first (get-hash request)))]
    (if (BCrypt/checkpw password hash)
      {:username username}
      (client-error "Bad Password"))
    (client-error "User not found.")))
