(ns subticket.dbcon
  (:require [environ.core :refer [env]]
            [korma.db :refer [defdb get-connection]]))

(defdb thedb (assoc env :user (:db-user env) :password (:db-password env) :make-pool? true))

(defn db-connection [] (get-connection thedb))


