# chocolate

A simple test harness for playing with RabbitMQ using EDN and Protocol Buffers

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

You will also need to add `dev-config.edn` to the main project folder, containing:

```
{:dev true
 :port 3000
 :nrepl-port 7000

 :database-url "jdbc:sqlite:chocolate_dev.db"}
```
to make the system happy. (I'm not going to add this file to this repo, just make your own copy)

## Running

To start the server for the application, run:

    lein run 

Then, in another terminal/powershell window, run the client:

    lein figwheel


