(ns subticket.dbcon
  (:require [environ.core :refer [env]]
            [korma.db :refer [defdb get-connection]]
            [io.pedestal.log :as log]))
(def connection-settings (select-keys env [:classname :subprotocol :subname :user :password :db]))
(log/info :msg connection-settings)
(defdb thedb (assoc connection-settings :make-pool? true))

(defn db-connection [] (get-connection thedb))


