(ns chocolate.events
  (:require
    [re-frame.core :as rf]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [ajax.core :as ajax]
    [reitit.frontend.easy :as rfe]
    [reitit.frontend.controllers :as rfc]))

;;dispatchers

(rf/reg-event-fx
  :get-version
  (fn-traced [cofx _]
             (prn ":get-version")
             {:http-xhrio {:method          :get
                           :uri             "/api/version"
                           :response-format (ajax/json-response-format {:keywords? true})
                           :on-success      [:set-version]
                           :on-error [:version-error]}}))


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
  :common/set-error
  (fn-traced [db [_ error]]
    (assoc db :common/error error)))

(rf/reg-event-fx
  :page/init-home
  (fn-traced [_ _]
    (rf/dispatch [:load-messages])
    (rf/dispatch [:load-consumers])
    (rf/dispatch [:load-protobuf-types])))


(rf/reg-event-db
  :message/add
  (fn-traced
    [db [_ {:keys [queue content] :as message}]]
    (prn ":message/add " queue "/" content)
    (cond

      (= queue "EDN") (do
                        (prn "EDN message")
                        (assoc db :edn-messages-received (conj (:edn-messages-received db) content)))

      (= queue "Person") (do
                           (prn "Person message")
                           (assoc db :person-messages-received (conj (:person-messages-received db) content)))

      (= queue "Message") (do
                            (prn "Message message")
                            (assoc db :message-messages-received (conj (:message-messages-received db) content))))))



(rf/reg-event-db
  :set-version
  (fn-traced [db [_ version]]
             ;(prn ":set-version " version)
             (assoc db :version (:version version))))


(rf/reg-event-db
  :version-error
  (fn-traced [db [_ error]]
    (assoc db :version error)))


;;subscriptions


(rf/reg-sub
  :edn-messages-received
  (fn [db _]
    (-> db :edn-messages-received)))

(rf/reg-sub
  :person-messages-received
  (fn [db _]
    (-> db :person-messages-received)))

(rf/reg-sub
  :message-messages-received
  (fn [db _]
    (-> db :message-messages-received)))

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
  :common/error
  (fn [db _]
    (:common/error db)))

(rf/reg-sub
  :version
  (fn [db _]
    ;(prn (str ":version " db))
    (get db :version)))

