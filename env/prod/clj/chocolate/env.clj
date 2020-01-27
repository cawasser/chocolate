(ns chocolate.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[chocolate started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[chocolate has shut down successfully]=-"))
   :middleware identity})
