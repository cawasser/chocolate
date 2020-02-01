(ns chocolate.protobuf.encoder
  (:require [protobuf.core :as protobuf]
            [chocolate.protobuf.interface :as pb-if])
  (:import [com.example.tutorial Example Example$Person Example$Message]))


(defn- preprocess-content
  "convert the string returned from the database into an EDN data structure for encoding.

   c - a string value that should be converted into EDN"

  [content]
  (clojure.core/read-string content))



(defmethod pb-if/encode-content "Person" [msg]
  (prn "Person encoder")
  (assoc msg :content (-> (protobuf/create Example$Person (preprocess-content (:content msg)))
                          (protobuf/->bytes))))



(defmethod pb-if/encode-content "Message" [msg]
  (prn "Message encoder")
  (assoc msg :content (-> (protobuf/create Example$Message (preprocess-content (:content msg)))
                        (protobuf/->bytes))))




(comment
  (def person {:id "3",
               :msg_type "pb",
               :exchange "pb-exchange",
               :queue "person.queue",
               :pb_type "Person",
               :content "{:id 108, :name \"Alice\", :email \"alice@example.com\"}"})

  (def message {:id "4",
                :msg_type "pb",
                :exchange "pb-exchange",
                :queue "message.queue",
                :pb_type "Message",
                :content "{:sender \"Alice\", :content \"Hello from Alice\", :tags [\"hello\" \"alice\" \"friends\"]}"})

  (:pb_type person)
  (clojure.core/read-string (:content person))

  (preprocess-content (:content person))

  (pb-if/encode-content person)

  (pb-if/encode-content message)

  ())
