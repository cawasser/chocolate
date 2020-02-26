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

    [chocolate.db.core :as db]
    [chocolate.message-publisher :as mp]
    [chocolate.message-consumer :as mc]
    [chocolate.routes.edn-utils :as e]
    [chocolate.protobuf.encoder :as pbe]))



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

   ["/messages"
    {:get {:summary   "return all messages in the database"
           :responses {200 {:body {:messages [{}]}}}
           :handler   (fn [_]
                        (ok {:messages (db/get-messages)}))}}]

   ["/consumers"
    {:get {:summary   "return all consumers in the database"
           :responses {200 {:body {:consumers [{}]}}}
           :handler   (fn [_]
                        (ok {:consumers (db/get-consumers)}))}}]

   ["/protobuf-types"
    {:get {:summary    "return all the protobuf-types"
           :responses  {200 {:body {:protobuf-types {}}}}
           :handler    (fn [_]
                         (ok {:protobuf-types (e/load-edn "resources/edn/protobuf-types.edn")}))}}]

   ["/publish"
    {:post {:summary    "publish a message"
            :responses  {200 {:body {:success boolean? :exchange string?}}}
            :parameters {:body {:id string?}}
            :handler    (fn [{{{:keys [id]} :body} :parameters}]
                          (prn "message " id " published")
                          (ok (mp/publish-message id)))}}]

   ["/publish-raw"
    {:post {:summary    "publish a message"
            :responses  {200 {:body {:success boolean? :exchange string?}}}
            :parameters {:body {:exchange string? :queue string?
                                :msg_type string? :pb_type string?
                                :content  string?}}
            :handler    (fn [{{{:keys [exchange] :as msg} :body} :parameters}]
                          (prn "raw message " msg " published to " exchange)
                          (ok (mp/publish-message-raw (pbe/preprocess-message msg))))}}]

   ["/start-consumer"
    {:post {:summary    "publish a message"
            :responses  {200 {:body {:success boolean? :exchange string?}}}
            :parameters {:body {:id string?}}
            :handler    (fn [{{{:keys [id]} :body} :parameters}]
                          (prn "starting consumer " id)
                          (ok (mc/start-consumer id)))}}]])



(comment
  (def id "100")

  ())



(comment
  (db/get-messages)
  (db/get-user {:id "200"})

  (db/clear-messages!)

  (db/create-user! {:id         "200",
                    :first_name "Steve",
                    :last_name  "Dallas",
                    :email      "steve@bloom.co",
                    :pass       "123ABc"})

  (do
    (db/create-message! {:id       "1"
                         :msg_type "edn"
                         :exchange "my-exchange"
                         :queue    "some.queue"
                         :pb_type  ""
                         :content  {:user "Chris"}})
    (db/create-message! {:id       "2"
                         :msg_type "edn"
                         :exchange "my-exchange"
                         :queue    "some.queue"
                         :pb_type  ""
                         :content  {:user "Steve"}})
    (db/create-message! {:id       "3"
                         :msg_type "pb"
                         :exchange "pb-exchange"
                         :queue    "person.queue"
                         :pb_type  "Person"
                         :content  {:id 108
                                    :name "Alice"
                                    :email "alice@example.com"}})
    (db/create-message! {:id       "4"
                         :msg_type "pb"
                         :exchange "pb-exchange"
                         :queue    "message.queue"
                         :pb_type  "Message"
                         :content  {:sender "Alice"
                                    :content "Hello from Alice"
                                    :tags ["hello" "alice" "friends"]}}))
  (db/get-messages)


  (do
    (db/create-consumer! {:id       "100"
                          :msg_type "edn"
                          :exchange "my-exchange"
                          :queue    "some.queue"
                          :pb_type  ""})
    (db/create-consumer! {:id       "200"
                          :msg_type "pb"
                          :exchange "pb-exchange"
                          :queue    "person.queue"
                          :pb_type  "Person"})
    (db/create-consumer! {:id       "300"
                          :msg_type "pb"
                          :exchange "pb-exchange"
                          :queue    "message.queue"
                          :pb_type  "Message"}))
  (db/get-consumers)

  ())