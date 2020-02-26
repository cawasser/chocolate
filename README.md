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

 :database-url "jdbc:sqlite:chocolate_dev.db"}
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

## Using Swagger-UI

The app also supports swagger-ui at

    localhost:3000/swagger-ui

specifically the `/api/messages` and `/api/publish` routes.

> Remember: `/api/publish` takes a message _id_ as it's only parameter, so understand what content  
> you want to send before using.


## Using the RabbitMQ Console

[link](/docs/working-with-rabbit-console.md)