(ns chocolate.message-consumer
  (:require [chocolate.routes.edn-utils :as e]
            [chocolate.amqp.jms.consumer]
            [chocolate.amqp.rabbit.consumer]
            [chocolate.protobuf.handlers :as h]
            [chocolate.processing :as proc]
            [chocolate.protobuf.utils :as utils]
            [clojure.edn :as edn]))



(defn- call-consumer
  [consume-fn exchange queue handler-fn msg_type]
  (if (empty? ((eval consume-fn) exchange queue handler-fn msg_type))
    false
    true))

(comment
  (def exchange "pe")
  (def queue "person.queue")
  (def msg_type "pb")
  (def pb_type "Person")
  (def dummy "")
  (def handler-fn (h/pb-handler proc/pb-processing-fn pb_type dummy))

  (empty? ())
  ())

(defn start-consumer-raw
  [consume-fn exchange queue msg_type pb_type dummy]

  (prn "start-consumer-raw " consume-fn "," exchange ", " queue ", " msg_type ", " pb_type ", " dummy)

  (let [ret {:exchange exchange :queue queue :msg-type msg_type}]
    (condp
     = msg_type

     "edn" (assoc ret :success (call-consumer
                                consume-fn
                                exchange
                                queue
                                (proc/edn-handler proc/edn-processing-fn)
                                msg_type))

     "pb" (assoc ret :success (call-consumer
                               consume-fn
                               exchange
                               queue
                               (h/pb-handler proc/pb-processing-fn pb_type (if (string? dummy) (clojure.edn/read-string dummy) dummy))
                               msg_type)))))


(defn start-consumer
  "starts a listener using the configuration data in the database with the given id

   id - the identifier of the kind of messages the client wishes to consumer. all relevant information
   about the consumer: the exchange, the queue, and the message encoding type, are taken form
   the database"

  [id]

  (if-let [{:keys [consume-fn exchange queue pb_type msg_type dummy]} (e/get-consumer {:id id})]
    (let [d (if (nil? dummy) (utils/get-from pb_type :dummy) dummy)]
     (do
       (prn "start-consumer " exchange ", " queue ", " msg_type ", " pb_type ", " dummy ", " d)
       (start-consumer-raw consume-fn exchange queue msg_type pb_type d)))

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


  (e/get-consumers)

  (e/get-consumer {:id "100"})

  (e/get-consumers-by-type {:msg_type "edn"})
  (e/get-consumers-by-type {:msg_type "pb"})

  (e/get-consumers-by-pb-type {:pb_type "Person"})
  (e/get-consumers-by-pb-type {:pb_type "Message"})


  (if-let [{:keys [exchange queue pb_type msg_type]} (e/get-consumer {:id id})]
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

  (if-let [{:keys [exchange queue pb_type msg_type]} (e/get-consumer {:id id})]
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





  (start-consumer "400")


  ())