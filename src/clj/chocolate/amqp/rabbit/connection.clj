(ns chocolate.amqp.rabbit.connection
  (:require [com.stuartsierra.component :as component]
            [bunnicula.component.connection :as connection]
            [bunnicula.component.publisher :as publisher]
            [bunnicula.protocol :as protocol]
            [bunnicula.component.monitoring :as monitoring]
            [bunnicula.component.consumer-with-retry :as consumer]
            ;[chocolate.config :refer [env]]
            [clojure.tools.logging :as log]))



;(defn make-connection []
;  (if (empty? (env :broker-url))
;    (do
;      (log/info "configuring " (env :broker-host) "/" (env :broker-port) "/" (env :broker-vhost))
;      (connection/create {:host (env :broker-host)
;                          :port (env :broker-port)
;                          :username (env :broker-username)
;                          :password (env :broker-password)
;                          :vhost (env :broker-vhost)}))
;    (do
;      (log/info "configuring " (env :broker-url) "/" (env :broker-vhost))
;      (connection/create {:url (env :broker-url)
;                          :vhost (env :broker-vhost)}))))

(defn create
  "Create a rabbitmq connection, based on a bunnicula connection component"
  [config]
  (connection/create config))




(comment

  (env :broker-vhost)
  (env :broker-url)

  (connection)
  ())



