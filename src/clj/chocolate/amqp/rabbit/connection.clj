(ns chocolate.amqp.rabbit.connection
  (:require [com.stuartsierra.component :as component]
            [bunnicula.component.connection :as conn]
            [bunnicula.component.publisher :as publisher]
            [bunnicula.protocol :as protocol]
            [bunnicula.component.monitoring :as monitoring]
            [bunnicula.component.consumer-with-retry :as consumer]
            [chocolate.config :refer [env]]
            [clojure.tools.logging :as log]))


(defonce conn-atom (atom ()))

(defn make-connection
  "Create a connection to a rabbitmq broker.
  Data is pulled from dev.edn"
  []
  (if (empty? (env :broker-url))
    (do
      ;(log/info "configuring " (env :broker-host) "/" (env :broker-port) "/" (env :broker-vhost))
      (conn/create {:host (env :broker-host)
                    :port (env :broker-port)
                    :username (env :broker-username)
                    :password (env :broker-password)
                    :vhost (env :broker-vhost)}))
    (do
      ;(log/info "configuring " (env :broker-url) "/" (env :broker-vhost))
      (conn/create {:url (env :broker-url)
                    :vhost (env :broker-vhost)}))))

(defn connection
  "Return an open connection, or create one if none exist"
  []
  (if (empty? @conn-atom)
    (do
      ;(log/info "opening broker connection")
      (reset! conn-atom (make-connection))))
  (log/info "returning broker connection " (:host @conn-atom))
  @conn-atom)


(comment

  (env :broker-vhost)
  (env :broker-url)

  (connection)
  ())



