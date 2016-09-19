(ns subticket.entities
  (:require
   [subticket.dbcon :refer [thedb]]
   [korma.core :as korma ]))

(korma/defentity users
  (korma/pk :username)
  (database thedb))
