(ns subticket.data-model
  (:require [clojure.spec.alpha :as s]))

(defn normal-length? [x] (<= (count x) 64))
(s/def ::username (s/and string? normal-length?))
(s/def ::password string?)

(s/def ::add-user (s/keys :req-un [::username ::password]))
(s/def ::login ::add-user)
