(ns subticket.users
  (:require subticket.dbcon
            [subticket.util :refer [client-error]]
            [yesql.core :as yesql]
            [io.pedestal.log :as log]))

(yesql/defqueries "sql/users.sql" subticket.dbcon/db)

(defn- add-pw-hash
  [request]
  (assoc request :pw_hash (:password request)))

(defn add-user-handler
  [request]
  (when (= 0 (add-user! (add-pw-hash request)))
    (client-error "User exists")))


