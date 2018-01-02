(ns subticket.data-model
  (:require [clojure.spec :as s]))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email (s/and string? #(re-matches email-regex %)))
(s/def ::username string?)
(s/def ::name string?)
(s/def ::password string?)

(s/def ::add-user (s/keys :req-un [::username ::password]))
(s/def ::login ::add-user)
