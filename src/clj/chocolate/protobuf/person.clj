(ns chocolate.protobuf.person
  (:require [protobuf.core :as protobuf]
            [chocolate.protobuf.interface :as pb-if]
            [chocolate.protobuf.encoder :as pbe])
  (:import [com.example.tutorial Example Example$Person]))



(def dummy-person {:id 0 :name "dummy" :email "dummy"})


(defmethod pb-if/encode-content "Person" [msg]
  (prn "Person encoder")
  (assoc msg :content (->> msg
                        (:content)
                        (pbe/preprocess-content)
                        (protobuf/create Example$Person)
                        (protobuf/->bytes))))



(defmethod pb-if/decode-content "Person" [pb_type msg]
  (prn "Person decoder")
  (->> msg
    (protobuf/bytes-> (protobuf/create Example$Person dummy-person))))




(comment
  (def person {:id "3",
               :msg_type "pb",
               :exchange "pb-exchange",
               :queue "person.queue",
               :pb_type "Person",
               :content "{:id 108, :name \"Alice\", :email \"alice@example.com\"}"})

  (pb-if/decode-content "Person" (:content (pb-if/encode-content person)))

  ())