(ns chocolate.amqp.rabbit.publisher
  (:require [com.stuartsierra.component :as component]
            [bunnicula.component.publisher :as publisher]
            [bunnicula.protocol :as protocol]
            [chocolate.amqp.rabbit.connection :as conn]
            [chocolate.protobuf.encoder :as encoder]))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; PRIVATE STUFF
;

(def publishers (atom {}))


(defn- register-publisher
  "adds the 'service' to the 'publishers' atom, keyed by 'exchange', so we can find and reuse it later

   exchange - the name of the exchange handled by this publisher
   service - the 'stuart sierra component' that is managing this exchange"
  [exchange service]
  (swap! publishers assoc exchange service))


(defn- create-publisher-service
  "creates the 'stuart sierra component' that manages and executes the publisher

   publisher - the bunnicula.publisher to turn into a 'stuart sierra component' for later use

   return - 'stuart sierra component' for later use"
  [publisher]
  (component/system-map
    :rmq-connection (conn/connection)
    :publisher (component/using
                 publisher
                 [:rmq-connection])))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; support different serializers
;

(defmulti create-publisher (fn [exchange msg_type] msg_type))

(defmethod create-publisher "edn" [exchange msg_type]
  (publisher/create {:exchange-name exchange}))

(defmethod create-publisher "pb" [exchange msg_type]
  (publisher/create {:exchange-name exchange :serializer (fn [m] m)}))



(defn- get-publisher-for
  "get the publisher for a given exchange. creates on, of the given msg_type, if one is not
   found in the registry. if no msg_type provided, defaults to 'edn'

   exchange        - the name of the exchange. this is used to find (or create) the relevant publisher component
   msg_type (opt.) - a string denoting the type of encoding for the message content of this publisher

   returns - the 'stuart sierra component' used to publish the message content

   NOTE: the publisher component is automatically 'started' by this call if it needs to create the
   publisher, so you DON'T need to start it manually"

  ([exchange msg-type]
   (let [p   (get @publishers exchange)
         typ (if (nil? msg-type) "edn" msg-type)]
     (if (not p)
       (let [new-p          (create-publisher exchange typ)
             service        (create-publisher-service new-p)
             started-server (component/start-system service)]
         (register-publisher exchange started-server)
         started-server)
       p)))

  ([exchange] (get-publisher-for exchange "edn")))






(defn publish
  "publish a message, expressed as a map, on the given exchange/queue

   required keys:
   exchange - the name of the exchange
   queue    - the name of the queue
   content  - the message content to encode and send

   optional keys:
   msg_type - string identifying the type of encoding for the message content

       values: 'edn' - message content is edn and should be encoded using the Bunnicula default encoder (json)
               'pb'  - the message is already encoded into a binary 'protocol buffer'"

  [{:keys [exchange msg_type queue content]}]

  ;(prn "publishing message to " exchange "/" queue
  ;  " //// (msg_type) " msg_type
  ;  " //// (content) " content)

  (let [p (get-publisher-for exchange msg_type)]
    (if p
      (let [ret (protocol/publish
                  (:publisher p)
                  queue
                  content)]
        (if (nil? ret)
          true
          false))
      (do
        (prn "no publisher found! " exchange "/" msg_type)
        false))))





;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; THESE ARE NOT USED
;


(defn start-publisher-for
  "start an already existing publisher (useful in the REPL)

  NOTE: normally you do NOT need to call this function

  exchange - the name of the exchange supported by the publisher"
  [exchange]
  (component/start-system (get @publishers exchange)))

(defn stop-publisher-for
  "stop an already existing publisher (useful in the REPL)

   NOTE: normally you do NOT need to call this function

    exchange - the name of the exchange supported by the publisher"
  [exchange]
  (component/stop-system (get @publishers exchange)))

(defn clear-registry
  "clears all registered publishers form the registry, stopping them before they are removed"
  []
  (for [p @publishers]
    (component/stop-system p))
  (reset! publishers {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; RICH COMMENTS for the REPL
;

(comment

  (def exchange "pb-exchange")
  (def queue "message.queue")
  (def msg_type "pb")




  (protocol/publish
    (:publisher (get-publisher-for "my-exchange"))
    "some.queue"
    {:name "Chris" :value 1876})

  (publish {:exchange "my-exchange" :queue "some.queue"
            :msg_type "edn" :content {:name "Steve" :value 2319}})

  (publish {:exchange "my-exchange" :queue "some.queue"
            :content  {:name "Bob" :value 345667}})

  (clear-registry)


  ())



(comment
  (component/stop-system nil)

  (publisher/create {:exchange-name "my-exchange"})

  (create-publisher-service (publisher/create {:exchange-name "my-exchange"}))

  (get-publisher-for "my-exchange")
  (:publisher (get-publisher-for "my-exchange"))

  (get @publishers "my-exchange")

  (component/start-system (get-publisher-for "my-exchange"))
  (:publisher (component/start-system (get-publisher-for "my-exchange")))

  (start-publisher-for "my-exchange" "")



  ())


(comment

  (defmulti animal (fn [x] x))
  (defmethod animal "dog" [x m] (str "Woof " m))

  (animal "dog" "Hound")

  ())



(comment
  (get-publisher-for "my-exchange")
  (get-publisher-for "pb-exchange" "pb")


  (protocol/publish
    (:publisher (get-publisher-for "my-exchange"))
    "some.queue"
    {:name "chris"})

  (publish {:exchange "my-exchange" :queue "some.queue"
            :msg_type "edn" :content {:name "Steve" :value 2319}})


  ())