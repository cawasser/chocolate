(ns chocolate.amqp.jms.connection
  (:require
    [clojms.component.context :as context]))


(defn create
  [config]
  (context/create config))
