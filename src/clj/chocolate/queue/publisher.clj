(ns chocolate.queue.publisher
  (:require
    [chocolate.amqp.jms.publisher :as jms-pub]
    [chocolate.amqp.jms.publisher :as rab-pub]
    [com.stuartsierra.component :as component]))



;; AS OF RIGHT NOW EXCHANGE FOR JMS = DESTINATION/QUEUE





;
;
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; PRIVATE STUFF
;;
;
;(def publishers (atom {}))
;
;
;(defn- register-publisher
;  "adds the 'service' to the 'publishers' atom, keyed by 'exchange', so we can find and reuse it later
;
;   exchange - the name of the exchange handled by this publisher
;   service - the 'stuart sierra component' that is managing this exchange"
;  [exchange service]
;  (swap! publishers assoc exchange service))
;
;
;
;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; PUBLIC INTERFACE
;


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

  [broker {:keys [exchange msg_type queue content] :as config}]
  ;(cond broker
  ;      "rabbit"
  ;      (rab-pub/publish config)
  (broker config))
        ;"jms"
        ;(jms-pub/publish config)])
