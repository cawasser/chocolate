(ns chocolate.amqp.jms.publisher
  (:require
    [clojms.component.publisher :as pub]
    [clojms.protocol :as protocol]
    [chocolate.amqp.jms.connection :as conn]
    [com.stuartsierra.component :as component]))



(def publishers (atom {}))


(defn- register-publisher
  "adds the 'service' to the 'publishers' atom, keyed by 'destination', so we can find and reuse it later

   destination - the name of the exchange handled by this publisher
   service     - the 'stuart sierra component' that is managing this publisher"
  [destination service]
  (swap! publishers assoc destination service))


(defn- create-publisher-service
  "creates the 'stuart sierra component' that manages and executes the publisher

   publisher - the clojms.publisher to turn into a 'stuart sierra component' for later use

   return - 'stuart sierra component' for later use"
  [publisher]
  (component/system-map
    :context   (conn/connection)
    :publisher (component/using
                 publisher
                 [:context])))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; support different serializers
;


(defn create-publisher []
  (pub/create {}))



(defn- get-publisher-for
  "get the publisher for a given exchange. creates on, of the given msg_type, if one is not
   found in the registry. if no msg_type provided, defaults to 'edn'

   exchange        - the name of the exchange. this is used to find (or create) the relevant publisher component

   returns - the 'stuart sierra component' used to publish the message content

   NOTE: the publisher component is automatically 'started' by this call if it needs to create the
   publisher, so you DON'T need to start it manually"


  [queue]
  (let [pub   (get @publishers queue)]
     (if (not pub)
       (let [new-pub        (create-publisher)
             service        (create-publisher-service new-pub)
             started-server (component/start-system service)]
         (register-publisher queue started-server)
         started-server)
       ;; If a pub already exist at that destination, return it
       pub)))









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

  [{:keys [msg_type queue content]}]

  (let [pub (get-publisher-for queue)]
    (if (some? pub)
      (do
        ;(prn pub)
        (let [serializer (if (= msg_type "edn") nil (fn [m] (m)))]
          (if (nil? serializer)
            (let [ret (protocol/send-message-sync
                        (:publisher pub)
                        queue
                        content
                        {:serializer serializer})]
              (prn ret)
              (if (some? ret)
                true
                false)))))
      (do
        (prn "no publisher found! " queue "/" msg_type)
        false))))
