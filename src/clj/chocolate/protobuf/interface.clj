(ns chocolate.protobuf.interface
  (:require [chocolate.protobuf.utils :as utils]
            [protobuf.core :as protobuf]
            [chocolate.protobuf.encoder :as pbe]))


;(defmulti encode-content (fn [m] (:pb_type m)))

(defn encode-content

  [{:keys [pb_type] :as msg}]
  (prn "encode-content " pb_type)
  (let [java-class (utils/get-from pb_type :class)]
    (utils/import-by-name java-class)
    (prn "pb class " java-class  "/" (utils/class-for-name java-class))
    (assoc msg :content (->> msg
                             (:content)
                             (protobuf/create (utils/class-for-name java-class))
                             (protobuf/->bytes)))))



;(defmulti decode-content (fn [pb_type m] pb_type))

(defn decode-content
  [pb_type dummy msg]

  (prn "decode-content " pb_type " //// " dummy)
  (let [java-class (utils/get-from pb_type :class)]
    (utils/import-by-name java-class)
    (->> msg
         (protobuf/bytes->
           (protobuf/create (utils/class-for-name java-class) dummy)))))


(comment
  (def im-content {:sender {:id 100 :name "Chris"}
                   :content {:sender "Chris"
                             :content "Here is an embedded message"
                             :tags ["tag1"]}})
  (def dummy im-content)

  (def im-msg {:exchange "pb-exchange"
               :queue "im.queue"
               :msg_type "pb"
               :pb_type "IM"
               :content im-content})

  (def pb_type "IM")
  (utils/get-from pb_type :class)

  (def java-class "com.example.tutorial.Example$IM")
  (utils/import-by-name java-class)
  (utils/class-for-name (utils/get-from pb_type :class))

  (:pb_type im-msg)

  (def enc (encode-content im-msg))

  (decode-content "IM" (:content im-msg) (:content (encode-content im-msg)))
  (encode-content im-content)



  ())



(comment
  (def pb_type "Person")
  (def java-class "com.example.tutorial.Example$Person")

  (def msg {:exchange "test-exchange"
            :content {:id 100 :name "testing" :testing "testing"}})

  (def person {:id "3",
               :msg_type "pb",
               :exchange "pb-exchange",
               :queue "person.queue",
               :pb_type "Person",
               :content {:id 108, :name "Alice", :email "alice@example.com"}})


  (protobuf/create (utils/class-for-name java-class) (:content person))
  (encode-content person)

  ())