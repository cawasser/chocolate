(ns chocolate.message-publisher
  (:require [chocolate.db.core :as db]
            [chocolate.queue.publisher :as qp]
            [chocolate.protobuf.interface :refer [encode-content]]
            [chocolate.protobuf.encoder]))





(defn publish-message
  " publishes the stock message associated in the database with the given id

   id - the identifier of the message the client wishes to publish. all relevant information
   about the message: the exchange, the queue, and the message encoding type, are taken form
   the database"
  [id]
  (if-let [msg (db/get-message {:id id})]

    (do
      (prn "publish-message for " id
        " //// (msg) " msg)

      (condp
        = (:msg_type msg)

        "edn" (qp/publish msg)
        "pb" (qp/publish (encode-content msg))))
    false))




(comment
  (if-let [msg (db/get-message {:id "3"})]
    msg
    false)
  (db/get-message {:id "4"})

  (def person {:id "3",
               :msg_type "pb",
               :exchange "pb-exchange",
               :queue "person.queue",
               :pb_type "Person",
               :content "{:id 108, :name \"Alice\", :email \"alice@example.com\"}"})

  (def message {:id "4",
                :msg_type "pb",
                :exchange "pb-exchange",
                :queue "message.queue",
                :pb_type "Message",
                :content "{:sender \"Alice\", :content \"Hello from Alice\", :tags [\"hello\" \"alice\" \"friends\"]}"})

  (encode-content person)
  (encode-content (db/get-message {:id "3"}))

  (qp/publish (encode-content person))
  (qp/publish (encode-content (db/get-message {:id "3"})))

  (publish-message "1")
  (publish-message "3")


  ())