(ns subticket.service
  (:require [clojure.core.async :as a]
            [clojure.spec.alpha :as s]
            [environ.core :refer [env]]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route :as route]
            [io.pedestal.interceptor :refer [IntoInterceptor map->Interceptor]]
            [io.pedestal.interceptor.chain :as chain]
            [io.pedestal.interceptor.error :as error-int]
            [io.pedestal.log :as log]
            java-time
            [ring.middleware.session.cookie :as cookie]
            subticket.dbcon
            [subticket.users :as users]
            [subticket.util :as u])
  (:import [java.lang Exception Integer]
           javax.xml.bind.DatatypeConverter
           com.subticket.PercentDecoder))

(require 'subticket.data-model)

(defn- pipe [from to f]
  (a/go
    (let [v (a/<! from)]
      (a/>! to (f v))
      (a/close! to)))
  to)

(extend-type clojure.lang.Fn
  IntoInterceptor
  (-interceptor [t]
    (map->Interceptor {:enter (fn [context]
                                (let [include-fn (partial assoc context :response)
                                      response (t (:request context))]
                                  (if (satisfies? clojure.core.async.impl.protocols/ReadPort response)
                                    (pipe response (a/chan 1)  include-fn)
                                    (include-fn response))))})))

(defmethod print-method java.time.Instant
  [dt out]
  (.write out (str "#subticket/instant \"" (.toString dt) "\"")))
(defmethod print-dup java.time.Instant
  [dt out]
  (.write out (str "#subticket/instant \"" (.toString dt) "\"")))

(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok                (partial response 200))
(def bad-request       (partial response 400))
(def server-error      (response 500 nil))
(def unauthenticated   (response 403 nil))

(def param-keys [:edn-params :form-params :json-params :path-params :query-params ::params])

(defn- get-params [request] (apply merge (vals (select-keys request param-keys))))
(defn- spec-for [route] (s/get-spec (keyword "subticket.data-model" (name route))))

(defn- handle-errors
  [e with-context]
  (if (instance? Exception e)
    (do (log/error :exception e)
        (with-context server-error))
    (if-let [body (:client-error e)]
      (if (instance? Exception body)
        (do (log/info :exception body)
            (with-context (bad-request (.getMessage body))))
        (do (log/info :msg body)
           (with-context (bad-request body))))
      (do (log/error :msg e)
          (with-context server-error)))))

(def client-error-exception-handler
  (error-int/error-dispatch [ctx ex]
    [{:exception-type :com.subticket.ClientErrorException}]
    (assoc ctx :response (bad-request (.getMessage ex)))
    :else
    (assoc ctx :io.pedestal.interceptor.chain/error ex)))

(def validate-request
  {:name :validate-request
   :enter
   (fn [context]
     (log/trace :msg context)
     (let [request (:request context)
           params (get-params request)
           route (get-in context [:route :route-name])
           spec (spec-for route)
           errors (and (s/spec? spec) (not (s/valid? spec params)) (s/explain-str spec params))]
       (if errors
         (chain/terminate (assoc context :response (bad-request errors)))
         context)))})

(def decode-path-segment
  {:name :decode-path-segment
   :enter
   (fn [context]
     (update-in context [:request :path-params] (partial u/map-values #(PercentDecoder/decode %))))})

(def params-only
  {:name :params-only
   :enter
   (fn [context]
     (let [request (:request context)
           params (get-params request)]
       (assoc context ::tmp-request request :request params)))
   :leave
   (fn [context] (assoc context :request (::tmp-request context)))})

(def handle-exceptions
  {:name :handle-exceptions
   :leave
   (fn [context]
     (if-let [e (get-in context [:response :exception])]
       (handle-errors e (partial assoc context :response))
       context))})

(def body-response
  {:name :body-response
   :leave
   (fn [context]
     (let [response (:response context)]
       (if (:status response)
         context
         (assoc context :response (ok response)))))})

(defn- valid-session?
  [username expiration]
  (and (empty? username) (java-time/before? expiration (java-time/instant))))

(defn- extend-session
  [session]
  (log/trace :msg session)
  (assoc session :expiration (java-time/plus (java-time/instant) (java-time/seconds (Integer/parseInt (:subticket-session-length-in-seconds env))))))

(defn- ok?
  [context]
  (= 200 (get-in context [:response :status])))

(def authenticated
  {:name :authenticated
   :enter
   (fn [context]
     (let [username (get-in context [:request :session :username])
           expiration (get-in context [:request :session :expiration])]
       (if (valid-session? username expiration)
         (assoc-in context [:request ::params ::username] username)
         unauthenticated)))
   :leave
   (fn [context]
     (if (ok? context)
       (let [session (or (get-in context [:request :session]) {})]
         (assoc-in context [:response :session] (extend-session session)))
       context))})

(def add-user-to-session
  {:name :add-user-to-session
   :leave
   (fn [context]
     (if (ok? context)
       (let [username (get-in context [:request :session :username])
             new-user (get-in context [:response :body :ussername])]
         (assoc-in context [:response :session] (extend-session {:username new-user})))
       context))})

(defn logins [handler] [(body-params/body-params)
                        http/json-body
                        client-error-exception-handler
                        decode-path-segment
                        validate-request
                        add-user-to-session
                        body-response
                        handle-exceptions
                        params-only
                        (subticket.dbcon/in-transaction handler)])
(defn authenticated-transaction [handler] [(body-params/body-params)
                                           http/json-body
                                           client-error-exception-handler
                                           decode-path-segment
                                           validate-request
                                           authenticated
                                           body-response
                                           handle-exceptions
                                           params-only
                                           (subticket.dbcon/in-transaction handler)])
(defn unauthenticated-transaction [handler] [(body-params/body-params)
                                             http/json-body
                                             client-error-exception-handler
                                             decode-path-segment
                                             validate-request
                                             body-response
                                             handle-exceptions
                                             params-only
                                             (subticket.dbcon/in-transaction handler)])

;; Tabular routes
(def routes #{["/user/:username" :put (unauthenticated-transaction users/add-user-handler) :route-name :add-user]
              ["/login" :post (logins users/login) :route-name :login]})

;; Map-based routes
;(def routes `{"/" {:interceptors [(body-params/body-params) http/html-body]
;                   :get home-page
;                   "/about" {:get about-page}}})

;; Terse/Vector-based routes
;(def routes
;  `[[["/" {:get home-page}
;      ^:interceptors [(body-params/body-params) http/html-body]
;      ["/about" {:get about-page}]]]])

(def ^:private session-key (DatatypeConverter/parseHexBinary (:subticket-secret-key env)))
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
              ;; session as encrypted cookie
              ::http/enable-session {:store (cookie/cookie-store {:key session-key :readers clojure.core/*data-readers*})}

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

