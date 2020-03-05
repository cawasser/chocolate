(ns chocolate.routes.edn-utils
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))



; directly from https://clojuredocs.org/clojure.edn/read)
(defn load-edn
    "Load edn from an io/reader source (filename or io/resource)."
    [source]
    (try
      (with-open [r (io/reader source)]
        (edn/read (java.io.PushbackReader. r)))

      (catch java.io.IOException e
        {:error "Couldn't open '%s': %s\n" source (.getMessage e)})
      (catch RuntimeException e
        {:error "Error parsing edn file '%s': %s\n" source (.getMessage e)})))


(defn load-text-file
  "Load edn from an io/reader source (filename or io/resource)."
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
  (load-text-file "resources/proto/person.proto")


  ())