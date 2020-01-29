(ns chocolate.routes.services
  (:require
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [chocolate.middleware.formats :as formats]
    [chocolate.middleware.exception :as exception]
    [ring.util.http-response :refer :all]
    [clojure.java.io :as io]

    [chocolate.db.core :as db]))

(defn service-routes []
  ["/api"
   {:coercion   spec-coercion/coercion
    :muuntaja   formats/instance
    :swagger    {:id ::api}
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}

   ;; swagger documentation
   ["" {:no-doc  true
        :swagger {:info {:title       "my-api"
                         :description "https://cljdoc.org/d/metosin/reitit"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url    "/api/swagger.json"
              :config {:validator-url nil}})}]]

   ["/math"
    {:swagger {:tags ["math"]}}

    ["/plus"
     {:post {:summary "plus with spec body parameters"
             :parameters {:body {:x int?, :y int?}}
             :responses {200 {:body {:total int?}}}
             :handler (fn [{{{:keys [x y]} :body} :parameters}]
                        (prn "plus" x y)
                        {:status 200
                         :body {:total (+ x y)}})}}]
    ["/minus"
     {:post {:summary "minus with spec body parameters"
             :parameters {:body {:x int?, :y int?}}
             :responses {200 {:body {:total int?}}}
             :handler (fn [{{{:keys [x y]} :body} :parameters}]
                        (prn "minus" x y)
                        {:status 200
                         :body {:total (- x y)}})}}]
    ["/concat"
     {:post {:summary "plus with spec body parameters"
             :parameters {:body {:x int?, :y string?}}
             :responses {200 {:body {:total string? :worked? boolean?}}}
             :handler (fn [{{{:keys [x y]} :body} :parameters}]
                        (prn "concat" x y)
                        {:status 200
                         :body {:total (str x ", " y) :worked? true}})}}]]

   ["/messages"
    {:get {:summary   "return all messages in the database"
           :responses {200 {:body {:messages [{}]}}}
           :handler   (fn [_]
                        (ok {:messages (db/get-messages)}))}}]

   ["/publish"
    {:post {:summary   "publish a message"
            :responses {200 {:body {:messages boolean?}}}
            :parameters {:body {:id string?}}
            :handler   (fn [{{{:keys [id]} :body} :parameters}]
                         (prn "message " id " published")
                         (ok {:messages true}))}}]])



(comment
  (db/get-messages)
  (db/get-user {:id "100"})

  (db/create-user! {:id         "200",
                    :first_name "Steve",
                    :last_name  "Dallas",
                    :email      "steve@bloom.co",
                    :pass       "123ABc"})

  (db/create-message! {:id       "1"
                       :msg_type "edn"
                       :exchange "edn-exchange"
                       :queue    "edn-queue"
                       :content  {:user "Chris"}})
  (db/create-message! {:id "2"
                       :msg_type "edn"
                       :exchange "edn-exchange"
                       :queue "edn-queue"
                       :content {:user "Steve"}})


  ())