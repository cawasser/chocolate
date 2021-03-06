(ns chocolate.protobuf.encoder
  (:require [protobuf.core :as protobuf])
            ;[chocolate.protobuf.interface :as pb-if])
  (:import protobuf.examples.PersonOuterClass$Person
           protobuf.examples.MessageOuterClass$Message))


(defn preprocess-content
  "convert the string returned from the database into an EDN data structure for encoding.

   c - a string value that should be converted into EDN"

  [content]
  (clojure.core/read-string content))


(defn preprocess-message
  "convert the string returned from the database into an EDN data structure for encoding.

   c - a string value that should be converted into EDN"

  [message]
  (assoc message :content (preprocess-content (:content message))))


(comment
  (def person {:id "3",
               :msg_type "pb",
               :exchange "pb-exchange",
               :queue "person.queue",
               :pb_type "Person",
               :content "{:id 108, :name \"Alice\", :email \"alice@example.com\"}"})

  (def message
    {:id "4",
     :msg_type "pb",
     :exchange "pb-exchange",
     :queue "message.queue",
     :pb_type "Message",
     :content "{:sender {:id 108, :name \"Alice\", :email \"alice@example.com\"}, :content \"Hello from Alice\" :tags [\"tag-2\"]}"})

  (:pb_type person)
  (clojure.core/read-string (:content person))

  (preprocess-content (:content person))
  (preprocess-message person)

  (->> {:id 108, :name "Alice", :email "alice@example.com"}
    (protobuf/create PersonOuterClass$Person)
    (protobuf/->bytes))

  (->> "{:id 108, :name \"Alice\", :email \"alice@example.com\"}"
    (preprocess-content)
    (protobuf/create PersonOuterClass$Person)
    (protobuf/->bytes))

  (->> person
    (:content)
    (preprocess-content)
    (protobuf/create PersonOuterClass$Person)
    (protobuf/->bytes))

  (def person-msg (->> person
                    (:content)
                    (preprocess-content)
                    (protobuf/create PersonOuterClass$Person)
                    (protobuf/->bytes)))

  (def message-msg (->> message
                        (:content)
                        (preprocess-content)
                        (protobuf/create MessageOuterClass$Message)
                        (protobuf/->bytes)))

  (def rt-message (->> message-msg
                      (protobuf/bytes->
                        (protobuf/create MessageOuterClass$Message {}))))
  (type rt-message)
  rt-message


  (require '[chocolate.protobuf.interface :as pb-if])
  ;(pb-if/decode-content "Person" person-msg)

  (pb-if/encode-content person)
  (pb-if/encode-content message)

  (preprocess-content (:content person))

  (pb-if/decode-content "Message" (:content (pb-if/encode-content  message)))

  ())
