(ns subticket.users
  (:require
   [korma.core :as korma]
   [subticket.entities :refer [users]]
   [java-time :refer [instant]]))

(defn- add-user
  [user]
  (korma/insert
   users
   (korma/values (assoc (select-keys user [:username :name :password :email]) :updated (instant) ))))

(defn add-user-handler
  [request] (add-user request))

