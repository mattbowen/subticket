(defproject subticket "0.0.1-SNAPSHOT"
  :description "A radically simple ticketing system"
  :url "https://github.com/mattbowen/subticket"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [io.pedestal/pedestal.service "0.4.0"]

                 ;; Remove this line and uncomment one of the next lines to
                 ;; use Immutant or Tomcat instead of Jetty:
                 [io.pedestal/pedestal.jetty "0.4.0"]
                 ;; [io.pedestal/pedestal.immutant "0.4.0"]
                 ;; [io.pedestal/pedestal.tomcat "0.4.0"]

                 [ch.qos.logback/logback-classic "1.1.2" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.7"]
                 [org.slf4j/jcl-over-slf4j "1.7.7"]
                 [org.slf4j/log4j-over-slf4j "1.7.7"]
                 [environ "1.0.1"]
                 [com.mchange/c3p0 "0.9.2.1"] ; connection pooling
                 [clj-liquibase "0.5.3"]  ; migrations!
                 [oss-jdbc      "0.8.0"]  ; for Open Source JDBC drivers
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  :plugins [[lein-environ "1.0.1"]]
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "subticket.server/run-dev"]}
                   :dependencies [[io.pedestal/pedestal.service-tools "0.4.0"]]}
             :uberjar {:aot [subticket.server]}}
  :main ^{:skip-aot true} subticket.server)

