(ns chocolate.amqp.jms.consumer
  (:require
    [clojms.component.consumer :as consumer]
    [clojms.client.jms.consumer :as jms]
    [chocolate.amqp.jms.connection :as conn]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [clojms.protocol :as protocol]
    [chocolate.routes.websockets :as ws]))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; PRIVATE STUFF
;

(def consumers (atom {}))


(defn- consumer-name [queue]
  (str queue "-consumer"))


(defn- find-consumer-for [queue]
  (get @consumers (consumer-name  queue)))


(defn- register-consumer
  "adds the 'service' to the 'consumers' atom, keyed by 'exchange', so we can find and reuse it later

   service - the 'stuart sierra component' that is managing this exchange"
  [queue service]
  (swap! consumers assoc (consumer-name queue) service))

(defn- create-consumer-service
  "creates the 'stuart sierra component' that manages and executes the consumer

   consumer - the bunnicula.consumer to turn into a 'stuart sierra component' for later use

   return - 'stuart sierra component' for later use"
  [consumer]
  (component/system-map
    :context (conn/connection)
    :consumer (component/using
                consumer
                [:context])))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; support different serializers
;

(defmulti create-consumer (fn [queue handler-fn msg_type] msg_type))

(defmethod create-consumer "edn" [queue handler-fn msg_type]
  (consumer/create {:destination  queue
                    :deserializer nil
                    :options      {:queue-name               queue
                                   :timeout-seconds          120
                                   :backoff-interval-seconds 60
                                   :consumer-threads         4
                                   :max-retries              3}}))

(defmethod create-consumer "pb" [queue handler-fn msg_type]
  (consumer/create {:destination  queue
                    :deserializer (fn [m] m)
                    :options      {:queue-name               queue
                                   :timeout-seconds          120
                                   :backoff-interval-seconds 60
                                   :consumer-threads         4
                                   :max-retries              3}}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; PUBLIC INTERFACE

(defn create-consumer-for
  "create a consumer for a given exchange. creates one, of the given msg_type, if one is not
   found in the registry. if no msg_type provided, defaults to 'edn'

   queue           - name of the queue to consume from
   hander-fn       - the function that will be called for every message received from the exchange/queue
   msg_type (opt.) - a string denoting the type of decoding for the message content of this consumer

   returns - the 'stuart sierra component' used to consume the queue's content

   NOTE: the consumer component is automatically 'started' by this call if it needs to create the
   consumer, so you DON'T need to start it manually

   NOTE: creating a second consumer on the same exchange/queue pair seems to \"replace\" the consumer wth the newer one. It is
   unclear if the original is garbage collected or simply leaked."

  ([exchange queue  handler-fn msg-type]
   (let [cons  (find-consumer-for queue)
         typ   (if (nil? msg-type) "edn" msg-type)
         decoder (if (< 0(compare typ "pb"))
                   (handler-fn)
                   nil)]
     ;(prn queue)
     ;(prn  handler-fn)
     ;(prn  cons)
     (try
       (if (not cons)
         (let [new-cons       (create-consumer queue handler-fn typ)
               service        (create-consumer-service new-cons)
               started-server (component/start-system service)]
           (register-consumer queue started-server)
           (let [msg           (protocol/receive-message-sync (:consumer started-server))
                 decoded-msg   (if decoder (decoder msg) msg)
                 msg-type-caps (.toUpperCase msg-type)
                 msg-to-send   {:content decoded-msg
                                :queue msg-type-caps}]
             ;(prn msg)
             (ws/send-to-all! msg-to-send))
           started-server)
         (do
           (let [msg  (protocol/receive-message-sync (:consumer cons))
                 decoded-msg   (if decoder (decoder msg) msg)
                 msg-type-caps (.toUpperCase msg-type)
                 msg-to-send   {:content decoded-msg
                                :queue msg-type-caps}]
             ;(prn msg)
             (ws/send-to-all! msg-to-send))
           cons))
       (catch Exception e (do
                            (log/debug "Can't connect consumer: " (.getMessage e))
                            ())))))

  ([exchange queue handler-fn] (create-consumer-for exchange queue handler-fn "edn")))





(defn stop-consumer-for [queue]
  (if-let [consumer (find-consumer-for queue)]
    (component/stop-system consumer)))

(defn remove-consumer-for [queue]
  (if-let [consumer (find-consumer-for queue)]
    (reset! consumers (dissoc @consumers (consumer-name queue)))))

(defn stop-and-remove-consumer-for [queue]
  (stop-consumer-for queue)
  (remove-consumer-for queue))

(defn stop-and-remove-all-consumers []
  (do (map (fn [[k v]]
             (component/stop-system v)
             (swap! consumers dissoc k))
           @consumers)))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; RICH COMMENTS for the REPL
;

;
; edn messages
;
(comment

  (require '[chocolate.queue.publisher :as pub])
  (require '[chocolate.message-publisher :as mp])
  (require '[chocolate.processing :as proc])


  (def exchange "my-exchange")
  (def queue "some.queue")
  (def typ "edn")
  (def msg-type "edn")
  (def handler-fn proc/edn-handler)

  (find-consumer-for exchange queue)

  (create-consumer exchange queue handler-fn typ)
  (def new-p (create-consumer exchange queue handler-fn typ))

  (create-consumer-service new-p)
  (def service (create-consumer-service new-p))

  (def started-server (component/start-system service))

  (register-consumer exchange queue started-server)
  (stop-consumer-for exchange queue)
  (remove-consumer-for exchange queue)
  @consumers

  (pub/publish {:exchange "my-exchange" :queue "some.queue"
                :msg_type "edn" :content {:name "Steve" :value 2319}})
  (pub/publish {:exchange "my-exchange" :queue "some.queue"
                :msg_type "edn" :content {:name "Dave" :value 2187}})

  (create-consumer-for "my-exchange" "some.queue" proc/edn-handler)

  (mp/publish-message "1")

  @proc/edn-messages-received
  (reset! edn-messages-received [])

  (stop-and-remove-consumer-for exchange queue)
  (stop-and-remove-consumer-for "my-exchange" "some.queue")

  ())


;
; protobuf messages
;
(comment

  (require '[chocolate.message-publisher :as mp])

  (def exchange "pb-exchange")
  (def queue "person.queue")
  (def typ "pb")
  (def handler-fn (h/pb-handler "Person"))

  (mp/publish-message "3")                                  ; person
  (mp/publish-message "4")                                  ; message

  (create-consumer-for exchange queue handler-fn typ)
  (create-consumer-for exchange "message.queue" (h/pb-handler "Message") typ)

  @h/messages-received

  (stop-and-remove-all-consumers)

  ())



(comment


 (def cons1 (create-consumer "some" #() "edn"))
 (:destination cons1)
 (ws/send-to-all! (protocol/receive-message-sync cons1))



 ())






