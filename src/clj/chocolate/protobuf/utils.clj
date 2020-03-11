(ns chocolate.protobuf.utils
  (:require [chocolate.routes.edn-utils :as e])
  (:import (clojure.lang RT)))


(defonce pb-type-reg (atom {}))

(defn import-by-name [name]
  (.importClass (the-ns *ns*) (RT/classForName name)))


(defn class-for-name [name]
  (RT/classForName name))


(defn get-from [pb-type item]
  (if (empty? @pb-type-reg)
    (reset! pb-type-reg (e/load-edn "edn/protobuf-types.edn")))

  (get-in @pb-type-reg [pb-type item]))




(comment
  (def pb-type "Person")
  (def item :class)

  (reset! pb-type-reg {})


  (def java-class "com.example.tutorial.Example$IM")
  (import-by-name java-class)

  (import '[com.example.tutorial.Example$IM])
  (import '[com.example.tutorial.Example$Person])


  ())