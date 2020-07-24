(ns chocolate.queue.consumer
  (:require [clojure.tools.logging :as log]
          [com.stuartsierra.component :as component]
          [chocolate.amqp.rabbit.connection :as conn]
          [chocolate.protobuf.handlers :as h]))









;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; PUBLIC INTERFACE

(defn create-consumer-for
  "create a consumer for a given exchange. creates one, of the given msg_type, if one is not
   found in the registry. if no msg_type provided, defaults to 'edn'

   exchange        - the name of the exchange. this is used to find (or create) the relevant publisher component
   queue           - name of the queue to consume from
   hander-fn       - the function that will be called for every message received from the exchange/queue
   msg_type (opt.) - a string denoting the type of decoding for the message content of this consumer

   returns - the 'stuart sierra component' used to consume the queue's content

   NOTE: the consumer component is automatically 'started' by this call if it needs to create the
   consumer, so you DON'T need to start it manually

   NOTE: bunnicula will automatically create a queue of the given name, so long as the exchange exists

   NOTE: creating a second consumer on the same exchange/queue pair seems to \"replace\" the consumer wth the newer one. It is
   unclear if the original is garbage collected or simply leaked."

  ([exchange queue handler-fn msg-type]
   (let [p   (find-consumer-for exchange queue)
         typ (if (nil? msg-type) "edn" msg-type)]
     (try
       (if (not p)
         (let [new-p          (create-consumer exchange queue handler-fn typ)
               service        (create-consumer-service new-p)
               started-server (component/start-system service)]
           (register-consumer exchange queue started-server)
           started-server)
         p)
       (catch Exception e (do
                            (log/debug "Can't connect consumer: " (.getMessage e))
                            ())))))

  ([exchange queue handler-fn] (create-consumer-for exchange queue handler-fn "edn")))



(defn stop-consumer-for [exchange queue]
  (if-let [consumer (find-consumer-for exchange queue)]
    (component/stop-system consumer)))

(defn remove-consumer-for [exchange queue]
  (if-let [consumer (find-consumer-for exchange queue)]
    (reset! consumers (dissoc @consumers (consumer-name exchange queue)))))

(defn stop-and-remove-consumer-for [exchange queue]
  (stop-consumer-for exchange queue)
  (remove-consumer-for exchange queue))

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

