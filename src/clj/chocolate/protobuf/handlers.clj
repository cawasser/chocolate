(ns chocolate.protobuf.handlers)




(defn pb-handler
  [processing-fn pb_type dummy]

  (prn "configuring pb-handler " pb_type ", " dummy)
  (fn [body parsed envelope components]
    (processing-fn pb_type dummy body parsed envelope components)))


