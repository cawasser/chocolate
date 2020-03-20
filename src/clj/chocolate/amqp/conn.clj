(ns chocolate.amqp.conn
  "Component holds open connection to RMQ"
  (:require [chocolate.amqp.rab-conn :as connection]
            [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [chocolate.config :refer [env]]))
  ;(:import (java.net URI)))

(defn- connection-url
  [{:keys [host port username password vhost]}]
  ;(env :qpid-url)
  ;"amqp://guest:guest@localhost:5672/default?brokerlist='tcp://localhost:5672'")
  ;"amqp://guest:guest@127.0.0.1:5672%2fmain"
  (let [ret (format "amqp://%s:%s@%s:%s/%s"
                    username password host port (string/replace vhost "/" "%2F"))]
    (log/info "connection url created: %s" ret)
    ret))
  ;(format "amqp://%s:%s@%s:%s/%s"
  ;     username password host port (string/replace vhost "/" "%2F")))


(defrecord Connection [url host port username password vhost connection-name  connection]
  component/Lifecycle
  (start [this]
    (if connection
      this ;; if a connection exist, it return this as the component
      (do  ;; else create a connection
          (if (some? url)
            (let [conn (connection/create url connection-name)]
              (log/infof "connection start, name=%s vhost=%s" connection-name vhost)
              (assoc this :connection conn))
            (let [new-url (connection-url {:host host
                                           :port port
                                           :username username
                                           :password password
                                           :vhost vhost})
                  conn (connection/create new-url connection-name)]
              (log/infof "connection start, name=%s vhost=%s" connection-name vhost)
              (assoc this :connection conn))))))
  (stop [this]
    (log/infof "connection stop, name=%s" connection-name)
    (when connection
      (connection/close connection))
    (assoc this :connection nil)))


(defn extract-server-config
  "If given a uri, parse it into a map of host port username and password
  Add in connection-name if not given it"
  [{:keys [url host port username password vhost connection-name]}]
  {:post [#(string? (:host %))
          #(string? (:port %))
          #(string? (:username %))
          #(string? (:password %))
          #(string? (:vhost %))]}
  (if-let [^java.net.URI uri (and url (java.net.URI. url))]
    (let [[username password] (string/split (.getUserInfo uri) #":")]
      {:host (.getHost uri)
       :port (.getPort uri)
       :username username
       :password password
       :connection-name (or connection-name username)
       :vhost vhost})
    {:host host
     :port port
     :username username
     :password password
     :connection-name (or connection-name username)
     :vhost vhost}))

(defn create
  "Create RabbitMQ connection for given connection config.
   Config can either contain ':url' key with full url
   or can be map with username, password, host and port values.
   Config always need to contain vhost!
   - {:url 'amqp://user:password@localhost:5492' :vhost 'main'}
   - {:username 'user' :password 'password' :host 'localhost' :port '5492':vhost 'main'}
   Optional config params
   - :connection-name
     - if not specified username is used as connection-name
     - {:url 'amqp://user:password@localhost:5492' :vhost 'main' :connection-name 'conn1'}"
  [config]
  ;(map->Connection (extract-server-config config)))
  (map->Connection config))



(comment

  (def cmap {:host (env :broker-host)
             :port (env :broker-port)
             :username (env :broker-username)
             :password (env :broker-password)
             :vhost (env :broker-vhost)})

  (connection-url cmap)
  (connection-url nil)


  (def cmap-url (connection-url cmap))
  ;=> "amqp://username:password@host:port/%2Fvhost"
  (def url (connection-url {:host "host"
                            :port "port"
                            :username "username"
                            :password "password"
                            :vhost "/vhost"}))

  (prn (^java.net.URI (java.net.URI. url)))
  (extract-server-config url)
  (extract-server-config cmap)
  cmap


  (def qpid-url (env :qpid-url))
  (def qpid-conn-name "qpid")
  (def qpid-conn (connection/create qpid-url qpid-conn-name))



  (def conn (connection/create cmap-url "hello"))


  (def stop-parinfer 1))






