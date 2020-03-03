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


(defn pb-processing-fn
  [pb_type dummy body parsed envelope components]

  (prn "running pb-processing-fn " pb_type ", " dummy ", " body)

  (let [decoded (pb-if/decode-content pb_type dummy body)]

    ; if this is a "Message", then republish onto the "EDN" queue (some.queue)
    (if (= pb_type "Message")
      (pub/publish-message-raw
        {:exchange "my-exchange" :queue "some.queue"
         :msg_type "edn" :content decoded}))

    (ws/send-to-all! (assoc {} :content decoded :queue pb_type)))
  :ack)





