(ns subticket.schema-migrations
  (:require [subticket.dbcon :refer [db-connection]]
            [clj-liquibase.cli    :refer [update]])
  (:use [clj-liquibase.core :refer (defparser)]))

(defparser changelog "migrations.edn")


(defn migrate-schema! []
  (update (assoc (db-connection) :changelog changelog)))