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
    [chocolate.message-publisher :as mp]))

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

   ["/publish"
    {:post {:summary    "publish a message"
            :responses  {200 {:body {:success boolean? :exchange string?}}}
            :parameters {:body {:id string?}}
            :handler    (fn [{{{:keys [id]} :body} :parameters}]
                          (prn "message " id " published")
                          (ok (mp/publish-message id)))}}]])



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



  ())