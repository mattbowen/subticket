(ns subticket.schema-migrations
  (:require [subticket.dbcon :refer [db-connection]]
            [clj-liquibase.cli :as cli]
            [clj-liquibase.core :refer [defparser]]))

(defparser changelog "migrations.edn")


(def schema 
  (assoc (db-connection) :changelog changelog))

(defn -main
  [& [cmd & args]]
  (apply cli/entry cmd schema args))
