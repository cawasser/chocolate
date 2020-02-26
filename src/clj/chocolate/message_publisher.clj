(ns chocolate.message-publisher
  (:require [chocolate.db.core :as db]
            [chocolate.queue.publisher :as qp]
            [chocolate.protobuf.interface :as pb]
            [chocolate.protobuf.encoder :as pbe]))


(defn publish-message-raw
  "published the given message, where the caller has specified everything"

  [{:keys [exchange queue msg_type] :as msg}]
  (prn "publish-message-raw for " exchange " / " queue
       " //// (msg) " msg)

  (let [ret {:exchange exchange :queue queue :msg-type msg_type}]
    (condp
      = (:msg_type msg)

      "edn" (assoc ret :success (qp/publish msg))
      "pb" (assoc ret :success (qp/publish (pb/encode-content msg))))))


(defn publish-message
  " publishes the stock message associated in the database with the given id

   id - the identifier of the message the client wishes to publish. all relevant information
   about the message: the exchange, the queue, and the message encoding type, are taken form
   the database"
  [id]
  (if-let [msg (db/get-message {:id id})]
    (-> msg
        (pbe/preprocess-message)
        (publish-message-raw))
    {:success false :id id}))




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

  (chocolate.protobuf.interface/encode-content (pbe/preprocess-message person))
  (chocolate.protobuf.interface/encode-content (pbe/preprocess-message (db/get-message {:id "3"})))

  (qp/publish (chocolate.protobuf.interface/encode-content
                (pbe/preprocess-message person)))
  (qp/publish (chocolate.protobuf.interface/encode-content
                (pbe/preprocess-message (db/get-message {:id "3"}))))

  (publish-message "1")
  (publish-message "3")


  ())


(comment
  (def one {:id "1",
            :msg_type "edn",
            :exchange "my-exchange",
            :queue "some.queue",
            :content "{:id 108, :name \"Alice\", :email \"alice@example.com\"}"})
  (def content (:content one))

  (def id "1")
  (def msg {:id "1", :msg_type "edn",
            :exchange "my-exchange", :queue "some.queue", :pb_type "",
            :content {:user "Chris"}})


  (qp/publish (encode-edn one))

  (publish-message "1")

  ())