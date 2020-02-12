(ns chocolate.processing
  (:require [chocolate.protobuf.interface :as pb-if]
            [chocolate.routes.websockets :as ws]))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; handler for EDN messages
;

(def edn-messages-received (atom []))


(defn edn-processing-fn
  [body parsed envelope components]
  (prn "edn-handler " parsed)
  (swap! edn-messages-received conj {:body body :converted parsed})

  ; send down the socket(s) to the client(s)
  (ws/send-to-all! parsed)

  :ack)



(defn edn-handler
  [processing-fn]
  (fn [body parsed envelope components]
    (processing-fn body parsed envelope components)))






;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; handler for Protobuf messages
;


(def pb-messages-received (atom []))

(defn pb-processing-fn
  [pb_type body parsed envelope components]

  (prn "pb-handler " (pb-if/decode-content pb_type body))
  (let [decoded (pb-if/decode-content pb_type body)]
    (swap! pb-messages-received conj {:body body :converted decoded})
    ; send down the socket(s) to the client(s)
    (ws/send-to-all! decoded))
  :ack)





