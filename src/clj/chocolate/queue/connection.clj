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
      (log/info "opening borker connection")
      (reset! conn-atom (connection/create {:host (env :broker-host)
                                            :port (env :broker-port)
                                            :username (env :broker-username)
                                            :password (env :broker-password)
                                            :vhost (env :broker-vhost)}))))

  (log/info "returning broker connection " (:host @conn-atom))
  @conn-atom)



(comment

  (env :broker-vhost)

  (connection)
  ())



