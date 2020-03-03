(ns chocolate.queue.connection
  (:require [com.stuartsierra.component :as component]
            [bunnicula.component.connection :as connection]
            [bunnicula.component.publisher :as publisher]
            [bunnicula.protocol :as protocol]
            [bunnicula.component.monitoring :as monitoring]
            [bunnicula.component.consumer-with-retry :as consumer]
            [chocolate.config :refer [env]]
            [clojure.tools.logging :as log]))

(defonce conn-atom (atom ()))

(defn connection []
  (if (empty? @conn-atom)
    (do
      (log/info "opening rabbitmq connection")
      (reset! conn-atom (connection/create {:host (env :rabbit-host)
                                            :port (env :rabbit-port)
                                            :username (env :rabbit-username)
                                            :password (env :rabbit-password)
                                            :vhost (env :rabbit-vhost)}))))

  (log/info "returning rabbitmq connection " (:host @conn-atom))
  @conn-atom)



(comment

  (env :rabbit-vhost)

  (connection)
  ())



