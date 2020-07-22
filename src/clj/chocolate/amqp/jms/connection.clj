(ns chocolate.amqp.jms.connection
  (:require
    [clojms.component.context :as context]
    [clojure.tools.logging :as log]
    [chocolate.config :refer [env]]))


(defonce conn-atom (atom ()))

(defn make-connection
  "Create a connection to a qpid broker.
  Data is pulled from dev.edn"
  []
  ;(log/info "configuring " (env :broker-url))
  (context/create {:url (env :broker-url-1)
                   :connection-name (or (env :connection-name) "qpid-connection")}))

(defn connection
  "Return an open connection, or create one if none exist"
  []
  (if (empty? @conn-atom)
    (do
      ;(log/info "opening broker connection")
      (reset! conn-atom (make-connection))))
  (log/info "returning broker connection " (:connection-name @conn-atom))
  @conn-atom)