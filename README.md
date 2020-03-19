# Chocolate

[![contributors](https://img.shields.io/github/contributors/cawasser/chocolate)](https://github.com/cawasser/chocolate/graphs/contributors)
[![activity](https://img.shields.io/github/commit-activity/m/cawasser/chocolate)](https://github.com/cawasser/chocolate/pulse)
[![clojure](https://img.shields.io/badge/made%20with-Clojure-blue.svg?logo=clojure)](https://clojure.org/)
[![version](https://img.shields.io/github/v/tag/cawasser/chocolate)](https://github.com/cawasser/chocolate/tags)

A simple test harness for playing with RabbitMQ using EDN and Protocol Buffers

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

### Development Setup and Configuration

You will also need to add `dev-config.edn` to the main project folder, containing:

```
{:dev true
 :port 3000
 :nrepl-port 7000

 :broker-host "127.0.0.1"
 :broker-port 5672
 :broker-username "guest"
 :broker-password "guest"
 :broker-vhost "/main"}
```
to make the system happy. (I'm not going to add this file to this repo, just make your own copy)

### RabbitMQ Configuration

Prior to running the app, you will need to install (not discussed here) _and_ run RabbitMQ. Then, using  
the management console at

    localhost:15672

You need to set up a vhost called `/main` and add the following exchanges to that vhost:

1. `my-exchange`
2. `pb-exchange`

Then add the following queues and bindings:

1. `some.queue` bound to `my-exchange`
2. `person.queue` bound to `pb-exchange`
3. `message.queue` bound to `pb-exchange`


## Compiling the Protocol Buffers

Before anything else, you need to compile the *.proto files (found [here](resources/proto)) into *.java files for further processing.

First you need to install the appropriate Protocol Buffer compiler `protoc` from [Google](https://github.com/protocolbuffers/protobuf/releases)  
for your operating system and make sure it is on your execution path.

(run `protoc --version` at the terminal/powershell to check)

To just compile the protocol buffers into Java,  run:

    lein protobuf


## Running the App

The lein-protobuf plugin automatically hooks into both `lein compile` and `lein run`

To compile everything and start the server for the application, in a terminal/powershell window, run:

    lein run 

Then, in another terminal/powershell window, run the client:

    lein figwheel

## Using the Client

[link](docs/client.md)

## Working Without a Database

This release of Chocolate does not use a database. All configuration information for both the server and the UI is contained in EDN files.

1. [protobuf-types.edn](resources/edn/protobuf-types.edn)
2. [consumer-types.edn](resources/edn/consumer-types.edn)
3. [publisher-types.edn](resources/edn/publisher-types.edn)

We will discuss each file separately.

#### protobuf.edn

The default content is:
``` clojure
{"Person" {:class "protobuf.examples.PersonOuterClass$Person"
           :protoc "resources/proto/person.proto"
           :dummy {}}
 "Message" {:class "protobuf.examples.MessageOuterClass$Message"
            :protoc "resources/proto/message.proto"
            :dummy {}}}
```
This file describes the mapping between the "short name" of the type (the key) and (as the value) the protocol buffer Java type (`:class`), the *.proto source
file (`:protoc`) and the "dummy" value needed by the [clojusc/protobuf](https://github.com/clojusc/protobuf) library for getting the
Java type back from it's binary representation (marshalling)

This file is used both internally by the marshalling and unmarshalling code as well as the user-facing "Flexible-publisher" and "Flexible-consumer" modal UI.

#### consumer-types.edn

The default content is:
``` clojure
[{:id       "100"
  :msg_type "edn"
  :exchange "my-exchange"
  :queue    "some.queue"
  :pb_type  ""}
 {:id       "200"
  :msg_type "pb"
  :exchange "pb-exchange"
  :queue    "person.queue"
  :pb_type  "Person"}
 {:id       "300"
  :msg_type "pb"
  :exchange "pb-exchange"
  :queue    "message.queue"
  :pb_type  "Message"}]
```
This file describes a collection (vector) of configuration data needed to establish an AMQP consumer using [Bunnicula](https://github.com/nomnom-insights/nomnom.bunnicula).
Each hash-map defines the _exchange_ and _queue_ for connecting to the broker. `:msg_type` and `:pb_type` are used to resolve
how each message in Clojure format should be handled. Our goal is to work in clojure/edn and leave the conversion into and out-of
protocol buffers to lower-levels of software.

`:msg_type` identifies EDN message, which require no additional processing. Bunnicula automatically marshalls the data into and
out-of JSON for transmission over AMQP.

`:pb_type` identifies the "short name" of the actual protocol buffer Java type to be used. This _must_ match the "short name" usedf
as the key in `protobuf-types.edn`'

Handlers are implemented for each consumer. By default, the handler decodes the received message and retransmits it to all connected
clients over websockets (using [sente](https://github.com/ptaoussanis/sente)). The handler for "Message" is different. In addition
to sending the decoded message to the clients, it also publishes the message onto _some.queue_. This means that all "Message"
messages will also be received by the "EDN" consumer (assuming one has been started).


#### publisher-types.edn

The default content is:
``` clojure
[{:id       "1"
  :msg_type "edn"
  :exchange "my-exchange"
  :queue    "some.queue"
  :pb_type  ""
  :content  {:user "Chris"}}
  
  ; ... elided 
  
 {:id       "3"
  :msg_type "pb"
  :exchange "pb-exchange"
  :queue    "person.queue"
  :pb_type  "Person"
  :content  {:id    108
             :name  "Alice"
             :email "alice@example.com"}}
             
  ;... elided 
]
```

This file describes a collection (vector) of configuration data needed to publish a message using [Bunnicula](https://github.com/nomnom-insights/nomnom.bunnicula).
Each hash-map defines the _exchange_ and _queue_ to publish the message on. `:msg_type` and `:pb_type` are used to resolve
how each message in Clojure format should be handled. Our goal is to work in clojure/edn and leave the conversion into and out-of
protocol buffers to lower-levels of software.

`:content` defines an EDN data structure of the actual message content to publish. In the case of `:msg_type = "edn"` the content
will be converted to JSON automatically by Bunnicula before publishing. In the case where `:msg_type = "pb"` the `:pb_type` value is
used to determine the specific protocol buffer Java type to use for the marshalling. The value of the `:pb_type` key _must_
match the "short name" in `protobuf-types.edn`.

> Note: "nested" protocol buffer types are defined using nested EDN data structures

### "Local" messages

In addition to the protobuf type data stored in the "default" files listed above, each file can have a _local_ variant. This allows
the develop to work on private, possibly proprietary, message formats without polluting the global repo.

| Default types         | "Optional" types (not in repo) |
|-----------------------|-----------------------------|
| `protobuf-types.edn`  | `protobuf-type-local.edn`   |
| `consumer-types.edn`  | `consumer-types-local.edn`  |
| `publisher-types.edn` | `publisher-types-local.edn` |


> NOTE: Do _*NOT*_ push the `*-local.edn` files to the repo!!!!!

*-local.edn files are entirely optional. If they are missing, the app still runs, just with only the base configuration.

## One More Thing...

In order to support additional "local" protocol buffer Java types, it is required for you to create a local
`local_protobuf.clj` file with the following content:

``` clojure
(ns chocolate.protobuf.local-protobuf
  (:require [your.local.protobuf.support.namespaces]))
```

Although you _*must*_ add this file to your local project (and _not_ push it to the repo), you only need to add
`:requires` for namespaces that help you with your "local" protobuf types. For example, if you have a protobuf type like:

``` protoc
import "DataPoint.proto"
message Sample
    string id = 1;
    repeated DataPoint = 2;
```

and you write a helper function to generate a large number of `DataPoint` instances to fill it:

``` clojure
(ns sample.helpers)

(defn make-data-point []
  {:data "some value"})
  
(defn make-sample [id num-points]
   {:id id :data-point (into [] (for [i (range num-points)] (make-data-point)))})   
```

you would add the "sample.sample" namespace as a dependency to `local_protobuf.clj`:

```
(ns chocolate.protobuf.local-protobuf
  (:require [sample.sample]))
```


> NOTE: Again, do _*not*_ push this file to the repo. You could be "leaking" proprietary information!

## Using Swagger-UI

The app also supports swagger-ui at

    localhost:3000/swagger-ui

specifically the `/api/messages` and `/api/publish` routes.

> Remember: `/api/publish` takes a message _id_ as it's only parameter, so understand what content  
> you want to send before using.


## Using the RabbitMQ Console

[link](/docs/working-with-rabbit-console.md)

## Building the Uberjar
Compiling the app into an uberjar allows it to be ran standalone locally* or within a docker container

Build the uberjar with

    lein uberjar

> Note: the uberjar process will take around 3-5 minutes to complete
>
>
To run the uberjar locally you first must set your environment variables

If you are on **Windows** run;

    .\dev\Invoke-CmdScript.ps1 .\dev\envVars.cmd

After the script executes, run `Get-ChildItem Env:` to verify environment variables are set, then run

    java -jar target/uberjar/chocolate.jar

which will run the application from the uberjar. go to http://localhost:3000

If you are on **Mac OS / Linux** run;

    ./dev/run.uberjar.sh
    
which will set env variables and run the uberjar.  go to http://locahost:3000


## Deploying with Docker
> Recommended way to run is with **docker-compose** (below).  
>The chocolate docker container by itself is not configured to talk to a locally running RabbitMQ instance

First build the chocolate uberjar

    lein uberjar

To build the chocolate container run:
    
    docker build -t chocodoc .

To run the container:

    docker run -p 3000:3000 chocodoc


## Deploying with Docker-Compose
The app can also be run using docker compose, which runs the chocolate container and RabbitMQ containers simultaneously.

First make sure you've built the chocolate uberjar: `lein uberjar`

To build the chocolate image run:
    
    docker build -t chocodoc .

> Note: the built image must be `-t` tagged `chocodoc` for the compose file to find it

Next build the rabbitMQ image with:

    docker build -t my-rabbit .\rabbitmq\.
    
> Note: built image must be -t tagged `my-rabbit` for compose to find it

To spin up the docker-compose containers run:

    docker-compose up
    
> Note: `ctrl-c` a few times will kill the running compose, and to completely clean and remove the containers, run `docker-compose down`

