(ns chocolate.protobuf.utils
  (:require [chocolate.routes.edn-utils :as e])
  (:import (clojure.lang RT)))


(defonce pb-type-reg (atom {}))

(defmacro import-by-name [name]
  `(import '[~name]))


(defn class-for-name [name]
  (RT/classForName name))


(defn get-from [pb-type item]
  (if (empty? @pb-type-reg)
    (reset! pb-type-reg (e/load-edn "resources/edn/protobuf-types.edn")))

  (get-in @pb-type-reg [pb-type item]))




(comment
  (def pb-type "Person")
  (def item :class)

  ())