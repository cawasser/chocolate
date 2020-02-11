(ns chocolate.routes.websockets
  (:require [clojure.tools.logging :as log]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [mount.core :as mount]
            [chocolate.middleware :as middleware]))



;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; SOCKET
;

(mount/defstate socket
  :start (sente/make-channel-socket!
           (get-sch-adapter) {:user-id-fn (fn [ring-req]
                                            (get-in ring-req [:params :client-id]))}))



(defn send! [uid message]
  ((:send-fn socket) uid message))



;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; HANDLERS (multimethods)
;

(defmulti handle-message (fn [{:keys [id]}] id))


(defmethod handle-message :default
  [{:keys [id]}]
  (log/debug "Received unrecognized websocket event type: " id))


; TODO: we don't actually receive any messages from the clients using the websockets... or do we?
(defmethod handle-message :message/create! [{:keys [?data uid] :as message}]
  (let [response (try
                   ;(msg/save-message! ?data)
                   (assoc ?data :timestamp (java.util.Date.)) (catch Exception e
                                                                (let [{id :error/error-id errors :errors} (ex-data e)]
                                                                  (case id :validation {:errors errors} ;;else
                                                                           {:errors
                                                                            {:server-error ["Failed to save message!"]}}))))]
    (if (:errors response)
      ; send error to "everyone"
      (send! uid [:message/creation-errors response])

      ; send "messages" to our connected users
      (doseq [uid (:any @(:connected-uids socket))]
        (send! uid [:message/add response])))))




;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; RECEIVER
;
;    calls the correct handler for the given message (type)

(defn receive-message!
  [{:keys [id] :as message}]
  (log/debug "Got message with id: " id)
  (handle-message message))



;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; SOCKET ROUTER
;

(mount/defstate channel-router
  :start (sente/start-chsk-router!
           (:ch-recv socket)
           #'receive-message!)
  :stop (when-let [stop-fn channel-router]
          (stop-fn)))



(defn websocket-routes
  []
  ["/ws"
   {:middleware [middleware/wrap-csrf middleware/wrap-formats]
    :get (:ajax-get-or-ws-handshake-fn socket)
    :post (:ajax-post-fn socket)}])

