(ns chocolate.message-publisher
  (:require [chocolate.db.core :as db]
            [chocolate.queue.publisher :as qp]))


(defmulti encode-content (fn [m] (:pb-type m)))



(defn publish-message
  " publishes the stock message associated in the database with the given id

   id - the identifier of the messages the client wishes to publish. all relevant information
   about the message: the exchange, the queue, and the message encoding type, are taken form
   the database"
  [id]
  (if-let [msg (db/get-message {:id id})]
    (condp
      = (:msg_type msg)
      "edn" (qp/publish msg)
      "pb" (qp/publish (encode-content msg)))))
