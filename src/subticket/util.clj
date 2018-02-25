(ns subticket.util)

(defn client-error [o] {:exception {:client-error o}})

(defn map-values [f m] (into {} (for [[k v] m] [k (f v)])))
