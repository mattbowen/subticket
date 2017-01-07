(ns subticket.entities
  (:require
   [subticket.dbcon :refer [thedb]]
   [korma.core :as korma ])
  (:import (java.sql Timestamp)))

(korma/defentity users
  (korma/pk :username)
  (korma/database thedb)
  (korma/entity-fields :name :email)
  (korma/prepare (fn [{updated :updated :as r}]
                   (assoc r :updated (Timestamp/from updated)))))
