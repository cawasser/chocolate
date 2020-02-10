(ns chocolate.protobuf.handlers
  (:require [chocolate.protobuf.interface :as pb-if]))



(def messages-received (atom []))

(defn pb-handler
  [pb_type]
  (fn
    [body parsed envelope components]
    (prn "pb-handler " (pb-if/decode-content pb_type body))
    (swap! messages-received conj {:body body :converted (pb-if/decode-content pb_type body)})
    :ack))


