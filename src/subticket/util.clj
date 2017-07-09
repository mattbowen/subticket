(ns subticket.util)

(defn client-error [o] {:exception {:client-error o}})
