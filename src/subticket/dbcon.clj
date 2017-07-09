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
(def ^:private tricky-db-spec
  (reify
    clojure.lang.ILookup
    (valAt [this o that]
      (if (= o :connection)
        conn
        that))
    (valAt [this o]
      (if (= o :connection)
        conn
        nil))
     clojure.lang.IPersistentMap
  (assoc [this _ _]
    this)
  (assocEx [this _ _]
    this)
  (without [this _]
    this)

  java.lang.Iterable
  (iterator [this]
    nil)

  clojure.lang.Associative
  (containsKey [_ k]
    (= k :connection))
  (entryAt [this o]
    (if (= o :connection)
      conn
      nil))

  clojure.lang.IPersistentCollection
  (count [_]
    1)
  (cons [this _]
    this)
  (empty [_]
    false)
  (equiv [this o]
    (= this 0))

  clojure.lang.Seqable
  (seq [_]
    nil)
  ))

(def db {:connection tricky-db-spec})

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
                         (.commit conn)
                         (when-not (nil? ret)
                           (>!! c ret)))
                       (catch Exception e
                         (.rollback conn)
                         (>!! c {:exception e}))
                       (finally
                         (.close conn)
                         (close! c)))))))
     c)))
