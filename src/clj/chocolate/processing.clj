(ns chocolate.processing
  (:require [chocolate.protobuf.interface :as pb-if]
            [chocolate.message-publisher :as pub]
            [chocolate.routes.websockets :as ws]))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; handler for EDN messages
;

(defn edn-processing-fn
  [body parsed envelope components]
  (ws/send-to-all! (assoc {} :content parsed :queue "EDN"))
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
  [pb_type body parsed envelope components]

  (let [decoded (pb-if/decode-content pb_type body)]

    ; if this is a "Message", then republish onto the "EDN" queue (some.queue)
    (if (= pb_type "Message")
      (pub/publish-message-raw
        {:exchange "my-exchange" :queue "some.queue"
         :msg_type "edn" :content decoded}))

    (ws/send-to-all! (assoc {} :content decoded :queue pb_type)))
  :ack)





