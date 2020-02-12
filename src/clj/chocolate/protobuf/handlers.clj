(ns chocolate.protobuf.handlers)




(defn pb-handler
  [processing-fn pb_type]
  (fn [body parsed envelope components]
    (processing-fn pb_type body parsed envelope components)))


