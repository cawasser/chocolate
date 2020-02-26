(ns chocolate.message-consumer
  (:require [chocolate.db.core :as db]
            [chocolate.queue.consumer :as qc]
            [chocolate.protobuf.handlers :as h]
            [chocolate.processing :as proc]))


(defn- call-consumer
  [exchange queue handler-fn msg_type]
  (if (nil? (qc/create-consumer-for exchange queue handler-fn msg_type))
    false
    true))

(defn start-consumer
  "starts a listener using the configuration data in the database with the given id

   id - the identifier of the kind of messages the client wishes to consumer. all relevant information
   about the consumer: the exchange, the queue, and the message encoding type, are taken form
   the database"

  [id]

  (if-let [{:keys [exchange queue pb_type msg_type]} (db/get-consumer {:id id})]

    (let [ret {:exchange exchange :queue queue :msg-type msg_type}]
      (prn "start-consumer " exchange "," queue "," pb_type "," msg_type)

      (condp
        = msg_type

        "edn" (assoc ret :success (call-consumer
                                    exchange
                                    queue
                                    (proc/edn-handler proc/edn-processing-fn)
                                    msg_type))

        "pb" (assoc ret :success (call-consumer
                                   exchange
                                   queue
                                   (h/pb-handler proc/pb-processing-fn pb_type)
                                   msg_type))))

    {:success false :id id}))




(comment

  (require '[chocolate.message-publisher :as mp])

  (mp/publish-message "1")

  (start-consumer "200")

  (def id "100")
  (def exchange "my-exchange")
  (def queue "some.queue")
  (def pb_type "Person")
  (def msg_type "pb")
  (def ret {})

  @proc/edn-messages-received

  @proc/pb-messages-received

  (qc/stop-and-remove-all-consumers)
  @qc/consumers


  (db/get-consumers)

  (db/get-consumer {:id "100"})

  (db/get-consumers-by-type {:msg_type "edn"})
  (db/get-consumers-by-type {:msg_type "pb"})

  (db/get-consumers-by-pb-type {:pb_type "Person"})
  (db/get-consumers-by-pb-type {:pb_type "Message"})


  (if-let [{:keys [exchange queue pb_type msg_type]} (db/get-consumer {:id id})]
    [exchange queue pb_type msg_type])

  (condp = msg_type
    "edn" "edn"
    "pb" "pb")

  (def ret {})

  (condp
    = msg_type

    "edn" (assoc ret :success
            (qc/create-consumer-for exchange queue proc/edn-handler msg_type))
    "pb" (assoc ret :success
           (qc/create-consumer-for
             exchange queue (h/pb-handler proc/pb-processing-fn pb_type) msg_type)))

  (if-let [{:keys [exchange queue pb_type msg_type]} (db/get-consumer {:id id})]
    (let [ret {:exchange exchange :queue queue :msg-type msg_type}]
      (condp
        = msg_type

        "edn" (assoc ret :success
                (if (nil? (qc/create-consumer-for exchange queue qc/edn-handler msg_type))
                  false true))
        "pb" (assoc ret :success
               (if (nil? (qc/create-consumer-for exchange queue (h/pb-handler pb_type) msg_type))
                 false true)))))

  (qc/stop-and-remove-all-consumers)

  ())