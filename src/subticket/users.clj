(ns subticket.users
  (:require
   [yesql.core :as yesql]
   [subticket.dbcon]))

(yesql/defqueries "sql/users.sql" subticket.dbcon/db)

(defn- add-pw-hash
  [request]
  (assoc request :pw_hash (:password request)))

(defn add-user-handler
  [request] (add-user! (add-pw-hash request)))

