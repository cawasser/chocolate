(ns chocolate.amqp.rab-conn
  (:require [clojure.tools.logging :as log])
  (:import (com.rabbitmq.client Connection ConnectionFactory)))

(defn create
  "Create rabbit connection for given rmq url
   - url needs to be in format 'amqp://username:passwored@host:port/vhost')
   - connection-name is NOT optional"
  [^String rmq-url connection-name]
  (let [factory (ConnectionFactory.)]
    (.setUri factory rmq-url)
    (.newConnection factory ^String connection-name)))

(defn close
  [^Connection conn]
  (if (.isOpen ^Connection conn)
    (.close ^Connection conn)))


(comment
  (def factory (ConnectionFactory.))

  (def url "amqp://guest:guest@localhost:5672/%2Fmain")
  (.setUri factory url)

  (def qpid-url "amqp://guest:guest@localhost:5672/default?brokerlist='tcp://localhost:5672'")
  (def qpid-url "amqp://guest:guest@localhost:5672/test?brokerlist='tcp://localhost:5672'?amqp.saslMechanisms='PLAIN'")
  (def qpid-url "amqp://guest:guest@localhost:5672/test?brokerlist='tcp://localhost:5672'?saslMechanisms='PLAIN'")
  (.setUri factory qpid-url)

  (.getHost factory)
  (.getPort factory)
  (.getUsername factory)
  (.getPassword factory)
  (.getVirtualHost factory)
  (.getMetricsCollector factory)
  (.getHandshakeTimeout factory)
  (.getNetworkRecoveryInterval factory)



  (def connection-name "test")
  (.newConnection factory ^String connection-name)


  (def stop 0))