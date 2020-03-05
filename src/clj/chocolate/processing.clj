(ns chocolate.processing
  (:require [chocolate.protobuf.interface :as pb-if]
            [chocolate.message-publisher :as pub]
            [chocolate.routes.websockets :as ws]))


(defonce edn-messages-received
         (atom []))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; handler for EDN messages
;

(defn edn-processing-fn
  [body parsed envelope components]
  (ws/send-to-all! (assoc {} :content parsed :queue "EDN"))
  (swap! edn-messages-received conj parsed)
  :ack)



(defn edn-handler
  [processing-fn]
  (fn [body parsed envelope components]
    (prn "edn-handler " parsed)
    (processing-fn body parsed envelope components)))






;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; handler for Protobuf messages
;

(defmulti pb-processing-fn
  "A protocol buffer processing function mustbe provided to a consumer. This hierarchy allows for
  both default handling of protobuf messages dynamicall (described by pb_type) and using specific
  handling logic by defining specific multimehods selceted by the value of pb_type.

  a :default implementation is provided which will handle ANY protobuf type dynamically, assuming
  the appropriate Java class is accessible on the classpath

  *pb_type*(string)      name of the protobuf type expected, used to load the Java class and
  instantiate an instance, required by bunnicula

  *dummy*(string)        edn string conating the required content to construct a valid instance of the
  Java class named by *pb_type*

  *body*(binary data)    the undecoded binary value of the protobuf message recevied on the queue,
  provided to the handler by bunnicaula

  *parsed*(string)       unused, provided to the handler by bunnicaula

  *envelope*(?)          unused, provided to the handler by bunnicaula

  *components*(?)        unused, provided to the handler by bunnicaula

  Note: since this is a multimethod, you can provide custom handlers (see (defmethod ... \"Message\" ...) below)"
  {:arglists '([pb_type dummy body parsed envelope components])}

  (fn [pb_type dummy body parsed envelope components] pb_type))



(defmethod pb-processing-fn :default
  [pb_type dummy body parsed envelope components]

  (prn "running pb-processing-fn " pb_type ", " dummy ", " body)

  (let [decoded (pb-if/decode-content pb_type dummy body)]
    (ws/send-to-all! (assoc {} :content decoded :queue pb_type)))
  :ack)



(defmethod pb-processing-fn "Message"
  [pb_type dummy body parsed envelope components]

  (prn "running pb-processing-fn " pb_type ", " dummy ", " body)

  (let [decoded (pb-if/decode-content pb_type dummy body)]

    (pub/publish-message-raw
      {:exchange "my-exchange" :queue "some.queue"
       :msg_type "edn" :content decoded})

    (ws/send-to-all! (assoc {} :content decoded :queue pb_type)))
  :ack)





