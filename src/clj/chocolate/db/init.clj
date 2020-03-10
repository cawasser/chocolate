(ns chocolate.db.init
    (:require
        [clojure.java.io :as jio]
        [chocolate.db.core :as db]
        [clojure.tools.logging :as log]))


(def db-name "chocolate_dev.db")

(defn init-message-db
    []
    (db/create-message! {:id       "1"
                         :msg_type "edn"
                         :exchange "my-exchange"
                         :queue    "some.queue"
                         :pb_type  ""
                         :content  {:user "Chris"}})
    (db/create-message! {:id       "2"
                         :msg_type "edn"
                         :exchange "my-exchange"
                         :queue    "some.queue"
                         :pb_type  ""
                         :content  {:user "Steve"}})
    (db/create-message! {:id       "3"
                         :msg_type "pb"
                         :exchange "pb-exchange"
                         :queue    "person.queue"
                         :pb_type  "Person"
                         :content  {:id 108
                                    :name "Alice"
                                    :email "alice@example.com"}})
    (db/create-message! {:id       "4"
                         :msg_type "pb"
                         :exchange "pb-exchange"
                         :queue    "message.queue"
                         :pb_type  "Message"
                         :content  {:sender "Alice"
                                    :content "Hello from Alice"
                                    :tags ["hello" "alice" "friends"]}}))



(defn init-consumer-db
  []
  (db/create-consumer! {:id       "100"
                        :msg_type "edn"
                        :exchange "my-exchange"
                        :queue    "some.queue"
                        :pb_type  ""})
  (db/create-consumer! {:id       "200"
                        :msg_type "pb"
                        :exchange "pb-exchange"
                        :queue    "person.queue"
                        :pb_type  "Person"})
  (db/create-consumer! {:id       "300"
                        :msg_type "pb"
                        :exchange "pb-exchange"
                        :queue    "message.queue"
                        :pb_type  "Message"}))


(defn initialize-db
    []
    (init-message-db)
    (init-consumer-db))


(defn database-exist?
  "Runs a simple check to see if the db file exists."
  [database-name]
  (.exists (jio/file (database-name))))


(defn setup-database
    "Only creates a database when in dev mode
     and when the chocolate_db has not been created."
    []
    (when (not (database-exist? db-name))
      ;; If it does not exist, create it
      (initialize-db)
      (log/info "creating database")))
