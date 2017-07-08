(ns subticket.dbcon
  (:require [clojure.core.async :refer [>!! chan close!]]
            [environ.core :refer [env]]
            [io.pedestal.log :as log])
  (:import com.mchange.v2.c3p0.ComboPooledDataSource
           [java.lang Exception Integer]
           [java.util.concurrent Executor Executors]
           javax.sql.DataSource))

(def ^:private db-spec {:classname "org.postgresql.Driver"
                          :subprotocol "postgresql"
                          :subname (str "//" (:subticket-db-hostname env) ":" (:subticket-db-port env) "/" (:subticket-db env))
                          :username (:subticket-db-user env)
                          :password (:subticket-db-pass env)})
(def ^:private pool-size (Integer/parseInt (:subticket-db-connections env)))

(defn- pool
  [spec]
  (doto (ComboPooledDataSource.)
    (.setDriverClass (:classname spec))
    (.setJdbcUrl (str "jdbc:" (:subprotocol spec) ":" (:subname spec)))
    (.setUser (:username spec))
    (.setPassword (:password spec))
    (.setMinPoolSize pool-size)
    (.setMaxPoolSize pool-size)
    (.setInitialPoolSize pool-size)))

(def pooled-db (delay (pool db-spec)))

;; this is an anti-pattern see https://stuartsierra.com/2013/03/29/perils-of-dynamic-scope
(def ^:dynamic ^:private conn)

(def db {:connection {:factory (fn [_] conn)}})

(defonce ^:private ^Executor db-executor
  (Executors/newFixedThreadPool pool-size))

(defn in-transaction
  "Takes a handler and returns a handler that executes with a bound db connection
  that is in a transaction."
  [f]
  (fn [req] (let [c (chan 1)]
     (let [binds (clojure.lang.Var/getThreadBindingFrame)]
       (.execute db-executor
                 (fn []
                   (clojure.lang.Var/resetThreadBindingFrame binds)
                   (binding [conn (.getConnection ^DataSource @pooled-db)]
                     (.setAutoCommit conn false)
                     (try
                       (let [ret (f req)]
                         (when-not (nil? ret)
                           (>!! c ret)
                           (.commit conn)))
                       (catch Exception e
                         (log/error :exception e)
                         (.rollback conn)
                         (>!! c {:exception e}))
                       (finally
                         (.close conn)
                         (close! c)))))))
     c)))
