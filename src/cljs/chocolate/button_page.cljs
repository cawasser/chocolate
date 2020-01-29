(ns chocolate.button-page
  (:require
    [day8.re-frame.http-fx]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [chocolate.ajax :as choco-ajax]
    [chocolate.events]
    [clojure.string :as string]
    [ajax.core :as ajax]))



(rf/reg-event-fx
  :load-messages
  (fn-traced [cofx [_]]
             {:http-xhrio {:method          :get
                           :uri             "/api/messages"
                           :format          (ajax/json-request-format)
                           :response-format (ajax/json-response-format {:keywords? true})
                           :on-success      [:messages-loaded]
                           :on-failure      [:common/set-error]}}))


(rf/reg-event-db
  :messages-loaded
  (fn-traced [db [_ messages]]
             (assoc db :messages (:messages messages))))



(rf/reg-sub
  :messages
  (fn [db [_]]
    (prn ":messages subscription " (:messages db))
    (:messages db)))




(defn button-page []
  (fn []
    (let [messages @(rf/subscribe [:messages])]
      [:ul
       (doall
         (map (fn [m] [:Li (str m)]) messages))])))