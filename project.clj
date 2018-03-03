(defproject subticket "0.0.1-SNAPSHOT"
  :description "A radically simple ticketing system"
  :url "https://github.com/mattbowen/subticket"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [io.pedestal/pedestal.service "0.5.3"]
                 [io.pedestal/pedestal.log "0.5.3"]

                   ;; Remove this line and uncomment one of the next lines to
                   ;; use Immutant or Tomcat instead of Jetty:
                 [io.pedestal/pedestal.jetty "0.5.3"]
                   ;; [io.pedestal/pedestal.immutant "0.4.0"]
                   ;; [io.pedestal/pedestal.tomcat "0.4.0"]

                 [ch.qos.logback/logback-classic "1.1.7" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.25"]
                 [org.slf4j/jcl-over-slf4j "1.7.25"]
                 [org.slf4j/log4j-over-slf4j "1.7.25"]
                 [environ "1.1.0"]
                 [com.mchange/c3p0 "0.9.5.2"] ; connection pooling
                 [migratus "1.0.3"]  ;; migrations!
                 [org.postgresql/postgresql "42.1.4"]
                 [yesql "0.5.3"]
                 [org.clojure/java.jdbc "0.7.5"]
                 [clojure.java-time "0.3.1"]
                 [ring/ring-core "1.6.3-SNAPSHOT"]
                 [org.mindrot/jbcrypt "0.4"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  :java-source-paths ["src/java" "test/java"]
  :plugins [[lein-environ "1.1.0"] [migratus-lein "0.5.4"]]
  :migratus {:store :database
             :migration-dir "migrations"
             :db {:classname "org.postgresql.Driver"
                  :subprotocol "postgresql"
                  :subname ~(str "//" (get (System/getenv) "SUBTICKET_DB_HOSTNAME") ":" (get (System/getenv) "SUBTICKET_DB_PORT") "/" (get (System/getenv) "SUBTICKET_DB"))
                  :user ~(get (System/getenv) "SUBTICKET_DB_USER")
                  :password ~(get (System/getenv) "SUBTICKET_DB_PASS")}}
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "subticket.server/run-dev"]}
                   :dependencies [[io.pedestal/pedestal.service-tools "0.5.1"]]}
             :uberjar {:aot [subticket.server]}}
  :main ^{:skip-aot true} subticket.server)

