;; This is a template. Environ uses it to connect to db. copy it to profiles.clj and update the values.
{:dev {:env {:classname "org.postgresql.Driver"
             :subprotocol "postgresql"
             :subname "//localhost:5432/subticket"
             :host "localhost"
             :port "5432"
             :user "USERNAME"
             :password "PASSWORD"
             :db "subticket"}}}
