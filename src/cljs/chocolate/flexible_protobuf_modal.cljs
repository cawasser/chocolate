(ns chocolate.flexible-protobuf-modal
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [ajax.core :as ajax]))



(rf/reg-event-db
  :selected-protobuf-type
  (fn-traced [db [_ new-val]]
    (assoc db
      :selected-protobuf-type new-val
      :selected-protoc (get-in db [:protobuf-types (keyword new-val)] {}))))


(rf/reg-event-fx
  :publish-message-raw

  (fn-traced [cofx [_ message]]
    (prn ":publish-message-raw " message)
    {:http-xhrio {:method          :post
                  :uri             "/api/publish-raw"
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :params          message
                  :on-success      [:message-published true]
                  :on-failure      [:message-published false]}}))



(rf/reg-sub
  :protobuf-types
  (fn [db [_]]
    (prn ":protobuf-types subscription " (:protobuf-types db))
    (keys (:protobuf-types db))))


(rf/reg-sub
  :selected-protoc
  (fn [db [_]]
    (prn ":protobuf-types subscription " (:protobuf-types db))
    (keys (:protobuf-types db))))


(rf/reg-sub
  :selected-protobuf-type
  (fn [db _]
    (:selected-protobuf-type db)))


(rf/reg-sub
  :selected-protoc
  (fn [db _]
    (:selected-protoc db)))



(defn input-field [tag type placeholder data]
  [:div.field
   [tag
    {:type        type
     :value       @data
     :placeholder placeholder
     :on-change   #(reset! data (-> % .-target .-value))}]])


(defn drop-down

  [items message]

  (if (< 0 (count items))
    [:select {:on-change #(rf/dispatch-sync [message (-> % .-target .-value)])}
     (for [[idx i] (map-indexed vector items)]
       (do
         (prn idx " / " i " / " (name i))
         ^{:key idx}[:option {:value i} (name i)]))]))

  ;(let [is-active (r/atom false)]
  ;  (fn []
  ;    [:div.dropdown (if @is-active {:class "is-active"})
  ;     [:div.dropdown-trigger
  ;      [:button.button {:on-click #(swap! is-active not)}
  ;       [:span "Content"]
  ;       [:span.icon.is-small
  ;        [:i.fas.fa-angle-down {:aria-hidden "true"}]]]]
  ;     [:div#dropdown-menu2.dropdown-menu {:role "menu"}
  ;      [:div.dropdown-content
  ;       [:div.dropdown-item
  ;        [:p "You can insert" [:strong "any type of content"] "within the dropdown menu."]]
  ;       [:div.dropdown-item
  ;        [:p "You simply need to use a" [:code "&lt;div&gt;"] "instead."]]
  ;       [:a.dropdown-item {:href "#"} "This is a link"]]]])))

(defonce exchange-name (r/atom ""))
(defonce queue-name (r/atom ""))
(defonce content-edn (r/atom ""))


(defn modal [is-active]
  (let [protobuf-types (rf/subscribe [:protobuf-types])
        selected-protobuf-type (rf/subscribe [:selected-protobuf-type])
        protoc (rf/subscribe [:selected-protoc])
        ready? #(and
                  (not (empty? @exchange-name))
                  (not (empty? @queue-name))
                  (not (empty? @content-edn)))]
    (fn []
      (prn "protobuf-types " @protobuf-types)

      [:div.modal (if @is-active {:class "is-active"})
       [:div.modal-background]
       [:div.modal-card
        [:header.modal-card-head
         [:p.modal-card-title "Specify Protobuf Message"]]

        [:section.modal-card-body ; exchange/queue
         [input-field :input.input :text "exchange" exchange-name]
         [input-field :input.input :text "queue" queue-name]]

        [:section.modal-card-body ; pb-type & protoc
         [drop-down @protobuf-types :selected-protobuf-type]
         [:p (:protoc @protoc)]]

        [:section.modal-card-body ; content-edn
         [input-field :input.input :textarea "content" content-edn]]
         ;[:p (if (ready?) "ready" "NOT")]]

        [:footer.modal-card-foot
         [:button.button.is-success {:disabled (not (ready?))
                                     :on-click #(rf/dispatch [:publish-message-raw
                                                              {:exchange @exchange-name
                                                                 :queue @queue-name
                                                                 :msg_type "pb"
                                                                 :pb_type @selected-protobuf-type
                                                                 :content @content-edn}])}
          "Publish"]
         [:button.button {:on-click #(reset! is-active false)} "Cancel"]]]])))
