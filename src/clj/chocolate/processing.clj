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
  "A protocol buffer processing function must be provided to a consumer. This function provides generic handling
  of ALL edn-formatted messages (converted to/from JSON automatically by bunnicula).

  *body* (binary)       unused, provided by bunnicula

  *parsed* (string)     EDN contents of the received message. bunnicula already decodes from JSON into EDN

  *envelope* (?)        unused, provided to the handler by bunnicaula

  *components* (?)      unused, provided to the handler by bunnicaula

  This is the \"base level\" handler for messages received on the queue. It does the work.

  In our case, it takes the decoded message and sends it to all attached clients using sente websockets. For REPL
  debugging purposes, it also conj's each message onto the `edn-messages` atom so we can see all the
  messages that have been received so far.
  "

  [body parsed envelope components]
  (ws/send-to-all! (assoc {} :content parsed :queue "EDN"))
  (swap! edn-messages-received conj parsed)
  :ack)



(defn edn-handler
  "A generic wrapper for a message handler function as required by bunnicula to properly handle received messages

  *processing-fn* (fn [body parsed envelope components])   the function to actually provide application specific logic
  for the handling of received messages. This function must take the 4 parameters sent by bunnicula"

  [processing-fn]
  (fn [body parsed envelope components]
    (prn "edn-handler " parsed)
    (processing-fn body parsed envelope components)))






;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; handler for Protobuf messages
;

(defmulti pb-processing-fn
  "A protocol buffer processing function must be provided to a consumer. This hierarchy allows for
  both default handling of protobuf messages dynamically (described by pb_type) and using specific
  handling logic by defining specific multimethods selected by the value of pb_type.

  a :default implementation is provided which will handle ANY protobuf type dynamically, assuming
  the appropriate Java class is accessible on the classpath

  *pb_type* (string)      name of the protobuf type expected, used to load the Java class and
  instantiate an instance, required by bunnicula

  *dummy* (string)        edn string containing the required content to construct a valid instance of the
  Java class named by *pb_type*

  *body* (binary data)    the un-decoded binary value of the protobuf message received on the queue,
  provided to the handler by bunnicaula. This function MUST decode the contents!

  *parsed* (string)       unused, provided to the handler by bunnicaula

  *envelope* (?)          unused, provided to the handler by bunnicaula

  *components* (?)        unused, provided to the handler by bunnicaula

  Note: since this is a multimethod, you can provide custom handlers (see (defmethod ... \"Message\" ...) below)"
  {:arglists '([pb_type dummy body parsed envelope components])}

  (fn [pb_type dummy body parsed envelope components] pb_type))


; this is the default method for processing ANY kind of ProtoBuf message
;
; pre-conditions: the relevant protobuf type, defined by pb_type MUST exist on the classpath
;
; This method will call the appropriate pb-if/decode-content function and pass the decoded message, assumed to be
; in EDN format, as provided by the [Bunnicula](https://github.com/nomnom-insights/nomnom.bunnicula) library,
; to all connected clients using sente websockets (via ws/send-to-all!)
;
; see the defmulti for descriptions of the parameters
;
(defmethod pb-processing-fn :default
  [pb_type dummy body parsed envelope components]

  (prn "running pb-processing-fn " pb_type ", " dummy ", " body)

  (let [decoded (pb-if/decode-content pb_type dummy body)]
    (ws/send-to-all! (assoc {} :content decoded :queue pb_type)))
  :ack)



; this is the method for processing protobuf messages of the "Message" class
;
; pre-conditions: the relevant protobuf type, defined by pb_type MUST exist on the classpath
;
; This method will call the appropriate pb-if/decode-content function and pass the decoded message, assumed to be
; in EDN format, as provided by the [Bunnicula](https://github.com/nomnom-insights/nomnom.bunnicula) library,
; to to the queue described by "my-exchange/some.queue" AND all connected clients using sente
; websockets (via ws/send-to-all!)
;
; see the defmulti for descriptions of the parameters
;
(defmethod pb-processing-fn "Message"
  [pb_type dummy body parsed envelope components]

  (prn "running pb-processing-fn " pb_type ", " dummy ", " body)

  (let [decoded (pb-if/decode-content pb_type dummy body)]

    (pub/publish-message-raw
      {:exchange "my-exchange" :queue "some.queue"
       :msg_type "edn" :content decoded})

    (ws/send-to-all! (assoc {} :content decoded :queue pb_type)))
  :ack)
