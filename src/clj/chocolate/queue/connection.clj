(ns chocolate.queue.connection
  (:require [com.stuartsierra.component :as component]
            [bunnicula.component.connection :as connection]
            [chocolate.amqp.conn :as conn]
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
      (case (env :broker) ;; Create a connection based on intended broker
            :qpid (do
                    (log/info "opening qpid connection")
                    (reset! conn-atom (conn/create {:url (env :qpid-url)
                                                    :connection-name "qpid-connection-name"})))
            :rabbit (do
                      (log/info "opening rabbit connection")
                      (reset! conn-atom (conn/create {:url nil
                                                      :host (env :broker-host)
                                                      :port (env :broker-port)
                                                      :username (env :broker-username)
                                                      :password (env :broker-password)
                                                      :vhost (env :broker-vhost)
                                                      :connection-name "rabbit-connection-name"}))))))

  (log/info "returning broker connection " (:connection-name @conn-atom))
  @conn-atom)



(comment

  (env :broker-vhost)
  (prn (env :broker-vhost))

  (connection)



  (case (env :broker)
    :qpid (do
            (prn "x"))
    :rabbit :y)
  ())



