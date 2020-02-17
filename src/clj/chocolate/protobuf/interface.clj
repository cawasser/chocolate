(ns chocolate.protobuf.interface)


(defmulti encode-content (fn [m] (:pb_type m)))


(defmulti decode-content (fn [pb_type m] pb_type))
