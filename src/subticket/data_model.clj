(ns subticket.data-model
  (:require [clojure.spec.alpha :as s]))

(defn check-length [size x] (<= (count x) size))
(def normal-length? (partial check-length 64))
(def long-length? (partial check-length 128))
(s/def ::username (s/and string? normal-length?))
(s/def ::email (s/and string? long-length?))
(s/def ::password string?)

(s/def ::add-user (s/keys :req-un [::username ::password ::email]))
(s/def ::login ::add-user)
