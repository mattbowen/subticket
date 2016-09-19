(ns subticket.users
  (:requre
   [korma.core :as korma]
   [subticket.entities :refer [users]]
   [java-time :refer [instant]]))

(defn add-user
  [user]
  (korma/insert
   users
   (values (assoc (select-keys user [:username :name :password :email]) :updated (instant) ))))

