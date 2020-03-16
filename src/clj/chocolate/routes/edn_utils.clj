(ns chocolate.routes.edn-utils
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))



; directly from https://clojuredocs.org/clojure.edn/read)
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



(comment

  (def source "dsa/edn/protobuf-types.edn")
  (load-edn "edn/protobuf-types.edn")

  (def r (io/resource source))
  (slurp r)
  (edn/read-string (slurp r))

  (if-let [r (io/resource source)]
    (edn/read-string (slurp r)))


  (load-text-file "resources/proto/person.proto")


  ())