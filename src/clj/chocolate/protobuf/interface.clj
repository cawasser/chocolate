(ns chocolate.protobuf.interface)


(defmulti encode-content (fn [m] (:pb_type m)))
