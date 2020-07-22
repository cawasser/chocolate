(ns chocolate.queue.connection
  (:require
    [chocolate.amqp.rabbit.connection :as rab-conn]
    [chocolate.amqp.jms.connection :as jms-conn]
    [clojure.tools.logging :as log]
    [chocolate.config :refer [env]]))



;;treated like an instance variable?
(defonce jms-conn-atom (atom ()))
(defonce conn-atom (atom ()))

(defn make-connection
  "Create a connection to either a rabbitmq broker or a amqp1.0 broker.
  Data is pulled from dev.edn"
  []
  (cond (env :broker)
        "rabbit"
        (if (empty? (env :broker-url))
          (do
            (log/info "configuring " (env :broker-host) "/" (env :broker-port) "/" (env :broker-vhost))
            (rab-conn/create {:host (env :broker-host)
                              :port (env :broker-port)
                              :username (env :broker-username)
                              :password (env :broker-password)
                              :vhost (env :broker-vhost)}))
          (do
            (log/info "configuring " (env :broker-url) "/" (env :broker-vhost))
            (rab-conn/create {:url (env :broker-url)
                              :vhost (env :broker-vhost)})))
        "jms"
        (do
          (log/info "Configuring" (env :broker-url) "jms connection")
          (jms-conn/create {:url (env :broker-url)
                            :name (env :connection-name)}))))

(defn connection
  "Return an open connection, or create one if none exist"
  []
  (if (empty? @conn-atom)
    (do
      (log/info "opening broker connection")
      (reset! conn-atom (make-connection))))
  (log/info "returning broker connection " (:host @conn-atom))
  @conn-atom)