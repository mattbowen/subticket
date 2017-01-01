(ns subticket.entities
  (:require
   [subticket.dbcon :refer [thedb]]
   [korma.core :as korma ]))

(korma/defentity users
  (korma/pk :username)
  (korma/database thedb)
  (korma/entity-fields :name :email))
