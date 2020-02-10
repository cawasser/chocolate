(ns chocolate.events
  (:require
    [re-frame.core :as rf]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [ajax.core :as ajax]
    [reitit.frontend.easy :as rfe]
    [reitit.frontend.controllers :as rfc]))

;;dispatchers

(rf/reg-event-db
  :navigate
  (fn-traced [db [_ match]]
    (let [old-match (:common/route db)
          new-match (assoc match :controllers
                                 (rfc/apply-controllers (:controllers old-match) match))]
      (assoc db :route new-match))))

(rf/reg-fx
  :navigate-fx!
  (fn-traced [[k & [params query]]]
    (rfe/push-state k params query)))

(rf/reg-event-fx
  :navigate!
  (fn-traced [_ [_ url-key params query]]
    {:navigate-fx! [url-key params query]}))

(rf/reg-event-db
  :set-docs
  (fn-traced [db [_ docs]]
    (assoc db :docs docs)))

(rf/reg-event-fx
  :fetch-docs
  (fn-traced [_ _]
    {:http-xhrio {:method          :get
                  :uri             "/docs"
                  :response-format (ajax/raw-response-format)
                  :on-success       [:set-docs]}}))

(rf/reg-event-db
  :common/set-error
  (fn-traced [db [_ error]]
    (assoc db :common/error error)))

(rf/reg-event-fx
  :page/init-home
  (fn-traced [_ _]
    (rf/dispatch [:load-messages])
    (rf/dispatch [:load-consumers])))

;;subscriptions

(rf/reg-sub
  :route
  (fn [db _]
    (-> db :route)))

(rf/reg-sub
  :page-id
  :<- [:route]
  (fn [route _]
    (-> route :data :name)))

(rf/reg-sub
  :page
  :<- [:route]
  (fn [route _]
    (-> route :data :view)))

(rf/reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))
