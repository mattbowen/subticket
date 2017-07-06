(ns subticket.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.interceptor.chain :as chain]
            [ring.util.response :as ring-resp]
            [subticket.users :as users]
            [clojure.spec :as s]
            [io.pedestal.log :as log]
            [subticket.data-model]
            [subticket.dbcon]))

(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok                (partial response 200))
(def bad-request       (partial response 400))

(def param-keys [:edn-params :form-params :json-params :path-params :query-params])

(defn- get-params [request] (apply merge (vals (select-keys request param-keys))))

(defn- spec-for [route] (keyword "subticket.data-model" (name route)))

(def default-json
  {:name :default-json
   :enter
   (fn [context]
     (let [request (:request context)
           params (get-params request)
           route (get-in context [:route :route-name])
           errors (s/explain-data (spec-for route) params)]
     (log/info :msg errors)
     (if errors
       (chain/terminate (assoc context :response (bad-request errors)))
       (assoc context ::tmp-request request :request params))))
   :leave
   (fn [context]
     (let [request (::tmp-request context)
           body (:response context)]
       (assoc context :request request :response (ok body))))})


;; Defines "/" and "/about" routes with their associated :get handlers.
;; The interceptors defined after the verb map (e.g., {:get home-page}
;; apply to / and its children (/about).
(defn json-interceptors [handler] [(body-params/body-params) http/json-body default-json (subticket.dbcon/in-transaction handler)])

;; Tabular routes
(def routes #{["/user/:username" :put (json-interceptors users/add-user-handler) :route-name :add-user]})

;; Map-based routes
;(def routes `{"/" {:interceptors [(body-params/body-params) http/html-body]
;                   :get home-page
;                   "/about" {:get about-page}}})

;; Terse/Vector-based routes
;(def routes
;  `[[["/" {:get home-page}
;      ^:interceptors [(body-params/body-params) http/html-body]
;      ["/about" {:get about-page}]]]])


;; Consumed by subticket.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ::http/type :jetty
              ;;::http/host "localhost"
              ::http/port 8080
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false}})

