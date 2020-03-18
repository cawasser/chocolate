(ns chocolate.routes.edn-utils
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))



; directly from https://clojuredocs.org/clojure.edn/read) and
; https://clojuredocs.org/clojure.java.io/resource
(defn load-edn
    "Load edn from an io/reader source (filename or io/resource)."
    [source]
    (try
      (if-let [r (io/resource source)]
        (edn/read-string (slurp r)))

      (catch java.io.IOException e
        {:error "Couldn't open '%s': %s\n" source (.getMessage e)})
      (catch RuntimeException e
        {:error "Error parsing edn file '%s': %s\n" source (.getMessage e)})))


(defn load-text-file
  "Load a text file into a string."
  [source]
  (try
    (-> source
        slurp)
        ;(clojure.string/replace "\r" "\n"))

    (catch java.io.IOException e
      {:error "Couldn't open '%s': %s\n" source (.getMessage e)})
    (catch RuntimeException e
      {:error "Error parsing edn file '%s': %s\n" source (.getMessage e)})))


(defn load-protobuf-types
  "load the standard set of protobuf-type along with any 'computer specific'
  addition. This was you can work with 'restricted' protobuf types without polluting
  the repo"

  []

  (merge (load-edn "edn/protobuf-types.edn")
         (load-edn "edn/protobuf-types-local.edn")))


(def messages (atom []))
(def consumers (atom []))

(defn get-messages []
  (if (empty? @messages)
    (reset! messages (into []
                           (concat
                             (load-edn "edn/publisher-types.edn")
                             (load-edn "edn/publisher-types-local.edn"))))
    @messages))

(defn get-message [{:keys [id]}]
  (first (filter #(= id (:id %)) (get-messages))))

(defn get-consumers []
  (if (empty? @consumers)
    (reset! consumers (into []
                            (concat
                              (load-edn "edn/consumer-types.edn")
                              (load-edn "edn/consumer-types-local.edn"))))
    @consumers))

(defn get-consumer [{:keys [id]}]
  (first (filter #(= id (:id %)) (get-consumers))))


(defn get-consumers-by-type [{:keys [msg_type]}]
  (into [] (filter #(= msg_type (:msg_type %)) (get-consumers))))

(defn get-consumers-by-pb-type [{:keys [pb_type]}]
  (into [] (filter #(= pb_type (:pb_type %)) (get-consumers))))





(comment

  (def source "dsa/edn/protobuf-types.edn")
  (load-edn "edn/protobuf-types.edn")

  (def r (io/resource source))
  (slurp r)
  (edn/read-string (slurp r))

  (if-let [r (io/resource source)]
    (edn/read-string (slurp r)))

  (load-text-file "resources/proto/person.proto")

  (load-protobuf-types)

  ; just to be sure things still work if the 'local' file is missing...
  (merge (load-edn "edn/protobuf-types.edn")
         (load-edn "edn/dummy.edn"))

  (get-consumers)
  (get-messages)

  (get-message {:id "1"})
  (get-consumer {:id "200"})

  (into [] (concat
             (load-edn "edn/publisher-types.edn")
             (load-edn "edn/dummy.edn")))

  ())