(ns chocolate.button-page
  (:require
    [day8.re-frame.http-fx]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [chocolate.ajax :as choco-ajax]
    [chocolate.events]
    [clojure.string :as string]
    [ajax.core :as ajax]

    [cljsjs.toastr]))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; message management
;

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


;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; consumer management
;

(rf/reg-event-fx
  :load-consumers
  (fn-traced [cofx [_]]
    {:http-xhrio {:method          :get
                  :uri             "/api/consumers"
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:consumers-loaded]
                  :on-failure      [:common/set-error]}}))


(rf/reg-event-db
  :consumers-loaded
  (fn-traced [db [_ consumers]]
    (assoc db :consumers (:consumers consumers))))


(rf/reg-sub
  :consumers
  (fn [db [_]]
    (prn ":consumers subscription " (:consumers db))
    (:consumers db)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; publish a message using the server
;

(rf/reg-event-fx
  :publish-message
  (fn-traced [cofx [_ id]]
    {:http-xhrio {:method          :post
                  :uri             "/api/publish"
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :params          {:id id}
                  :on-success      [:message-published true]
                  :on-failure      [:message-published false]}}))

; :dispatch-late expects the time in milliseconds (:ms)
;
(rf/reg-event-fx
  :message-published
  (fn-traced [cofx [_ success?]]
    (if success?
      (js/toastr.success "Published!")
      (js/toastr.error "Something went wrong..."))
    {}))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; publish a message
;

(defn publish-message [{:keys [id] :as message}]
  (rf/dispatch [:publish-message id]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; button-page
;

(defn button-page []
  (let [messages @(rf/subscribe [:messages])
        consumers @(rf/subscribe [:consumers])]
    (fn []
      [:div.level
       [:div.level-left {:style {:width "50%"}}
        [:h3 "Publish:"]
        [:div.tile.is-ancestor
         [:div.tile.is-vertical.is-8
          [:div.tile
           [:div.tile.is-parent.is-vertical
            (doall
              (map (fn [m] ^{:key (:id m)}
                            [:div.tile.is-child.box
                             {:on-click #(publish-message m)}
                             (str m)]) messages))]]]]]

       [:div.level-right {:style {:width "50%"}}
        [:h3 "Received:"]
        [:div.tile.is-ancestor
         [:div.tile.is-vertical.is-8
          [:div.tile
           [:div.tile.is-parent.is-vertical
            (doall
              (map (fn [m] ^{:key (:id m)}
                     [:div.tile.is-child.box
                      (str (:queue m))]) consumers))]]]]]])))