(ns subticket.dbcon
  (:require [environ.core :refer [env]]
            [korma.db :refer [defdb get-connection]]
            [io.pedestal.log :as log]))

(def connection-settings {:classname "org.postgresql.Driver"
                          :subprotocol "postgresql"
                          :subname (str "//" (:subticket-db-hostname env) ":" (:subticket-db-port env) "/" (:subticket-db env))
                          :username (:subticket-db-user env)
                          :password (:subticket-db-pass env)})
(log/info :msg connection-settings)
(defdb thedb (assoc connection-settings :make-pool? true))

(defn db-connection [] (get-connection thedb))


