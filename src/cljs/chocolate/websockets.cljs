(ns chocolate.websockets
  (:require-macros [mount.core :refer [defstate]])
  (:require [re-frame.core :as rf]
            [taoensso.sente :as sente]
            [mount.core]))




(defstate socket
  :start (do
           (prn "starting websocket " js/csrfToken)
           (sente/make-channel-socket!
             "/ws"
             js/csrfToken
             {:type :auto
              :wrap-recv-evs? false})))


; TODO: we don't send any messages to the server this way... or do we?
(defn send!
  [message]
  (if-let [send-fn (:send-fn @socket)]
    (send-fn message)
    (throw (ex-info "Couldn't send message, channel isn't open!"
                 {:message message}))))



;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; HANDLERS (multimethods)
;

; TODO: hook into the correct re-frame messages here

(defmulti handle-message (fn [{:keys [id]} _] id))

(defmethod handle-message :message/add
  [_ msg-add-event]
  (rf/dispatch msg-add-event))


(defmethod handle-message :message/creation-errors
  [_ [_ response]]
  (rf/dispatch
    [:form/set-server-errors (:errors response)]))



;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; DEFAULT handlers
;

(defmethod handle-message :chsk/handshake
  [{:keys [event]} _]
  (.log js/console "Connection Established: " (pr-str event)))

(defmethod handle-message :chsk/state
  [{:keys [event]} _]
  (.log js/console "State Changed: " (pr-str event)))

(defmethod handle-message :default
  [{:keys [event]} _]
  (.warn js/console "Unknown websocket message: " (pr-str event)))


;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; RECEIVER
;
;     calls the correct handler for the given message (type)

(defn receive-message!
  [{:keys [id event] :as ws-message}]
  (do
    (.log js/console "Event Received: " (pr-str event))
    (handle-message ws-message event)))


;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; COMPONENT
;
;     listens for messages from the server

(defstate channel-router
  :start (sente/start-chsk-router!
           (:ch-recv @socket)
           #'receive-message!)
  :stop (when-let [stop-fn @channel-router]
          (stop-fn)))
