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