(ns chocolate.protobuf.message
  (:require [protobuf.core :as protobuf]
            [chocolate.protobuf.interface :as pb-if]
            [chocolate.protobuf.encoder :as pbe])
  (:import [com.example.tutorial Example Example$Message]))


(def dummy-message {:sender "dummy" :content "dummy" :tags ["dummy"]})


(defmethod pb-if/encode-content "Message" [msg]
  (prn "Message encoder")
  (assoc msg :content (-> (->> msg
                            (:content)
                            (pbe/preprocess-content)
                            (protobuf/create Example$Message)
                            (protobuf/->bytes)))))



(defmethod pb-if/decode-content "Message" [pb_type msg]
  (prn "Person decoder")
  (->> msg
    (protobuf/bytes-> (protobuf/create Example$Message dummy-message))))




(comment
  (def message {:id "4",}
    :msg_type "pb",
    :exchange "pb-exchange",
    :queue "message.queue",
    :pb_type "Message",
    :content "{:sender \"Alice\", :content \"Hello from Alice\", :tags [\"hello\" \"alice\" \"friends\"]}")

  (pb-if/decode-content "Message" (:content (pb-if/encode-content message)))

  ())
