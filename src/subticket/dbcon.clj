(ns subticket.dbcon
  (:require [clojure.core.async :refer [>!! chan close!]]
            [environ.core :refer [env]])
  (:import com.mchange.v2.c3p0.ComboPooledDataSource
           [java.lang Exception Integer]
           [java.util.concurrent Executor Executors]
           javax.sql.DataSource))

(def ^:private db-spec {:classname "org.postgresql.Driver"
                          :subprotocol "postgresql"
                          :subname (str "//" (:subticket-db-hostname env) ":" (:subticket-db-port env) "/" (:subticket-db env))
                          :username (:subticket-db-user env)
                          :password (:subticket-db-pass env)})

(defn- pool
  [spec]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass (:classname spec))
               (.setJdbcUrl (str "jdbc:" (:subprotocol spec) ":" (:subname spec)))
               (.setUser (:user spec))
               (.setPassword (:password spec))
               (.setPoolMinSize (:subticket-db-connections env))
               (.setPoolMaxSize (:subticket-db-connections env)))]
    {:datasource cpds}))

(def pooled-db (delay (pool db-spec)))

;; this is an anti-pattern see https://stuartsierra.com/2013/03/29/perils-of-dynamic-scope
(def ^:dynamic ^:private conn)

(defn- bind-connection
  []
  (binding [conn (.getConnection ^DataSource @pooled-db)]))

(def db {:connection {:factory (fn [] conn)}})

(defonce ^:private ^Executor db-executor
  (Executors/newFixedThreadPool (Integer/parseInt (:subticket-db-connections env))))

(defn in-transaction
  "Takes a handler and returns a handler that executes with a bound db connection
  that is in a transaction."
  [f]
  (fn [req] (let [c (chan 1)]
     (let [binds (clojure.lang.Var/getThreadBindingFrame)]
       (.execute db-executor
                 (fn []
                   (clojure.lang.Var/resetThreadBindingFrame binds)
                   (.setAutoCommit conn false)
                   (try
                     (let [ret (f req)]
                       (when-not (nil? ret)
                         (>!! c ret)
                         (.commit c)))
                     (catch Exception e
                       (.rollback conn)
                       (>!! c {:exception e}))
                     (finally
                       (.close conn)
                       (close! c))))))
     c)))
