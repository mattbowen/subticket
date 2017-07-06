(ns subticket.users
  (:require
   [yesql.core :as yesql]
   [subticket.dbcon]))

(yesql/defqueries "sql/users.sql" subticket.dbcon/db)


(defn add-user-handler
  [request] (add-user! request))

