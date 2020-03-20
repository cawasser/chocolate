## Qpid

Qpid is an AMQP broker, similar to RabbitMQ.

### Installation 
Our project currently uses the qpid J Broker 7.1.8, available 
[here](http://apache.spinellicreations.com/qpid/broker-j/7.1.8/binaries/apache-qpid-broker-j-7.1.8-bin.zip).

It is recommended to unzip this package in ``C:\qpid``, which you will
have to create. After unzipping, be sure to create another directory,
```C:\qpid\qpidwork```.

The last thing to do after creating the ```\qpidwork``` directory is to
set this as the environment variable *QPID_WORK*. Set this to be 
```C:\qpid\qpidwork```.


### Config file
During the installation of the qpid broker you had to make a `qpidworks`
directory for the purposes of logging information from the server.
In this directory also sits a config file for the server, `config.json`.

In this config you must add the line `"secureOnlyMechanisms" : []`, 
in the `authenticationProviders` data structure. 

It should now resemble the following:
```
"authenticationproviders" : [ {
       "id" : "e16fb826-1ec4-4f11-804f-ac0689253314",
       "name" : "plain",
       "type" : "Plain",
       "users" : [ {
         "id" : "67d3784e-68a5-4129-8a1f-da58d2a0d396",
         "name" : "guest",
         "type" : "managed",
         "password" : "guest"
       } ],
       "secureOnlyMechanisms" : []
     } ]
```

### Running the server
To run the server, locate the ```\bin``` directory where you selected to
install qpid. Then run the following command in that directory:

```
qpid-server --initial-config-path "/config.json"
```

### Web Console
The qpid web console can be accessed at [localhost:8080](localhost:8080).

The login credentials are: 
```
Username: guest 
Password: guest
```

After signing in, the middle panel will have a "Broker" tab that shows
various stats about the qpid broker.

From the broker tab, scroll down to the "Virtual Hosts", section.

![picture](/images/virtual_hosts_tab.png "Virtual Hosts")

If there is not a virtual host, make sure to create one named "default"
with a *Virtual Host Node* Type of "JSON", and a *Virtual Node* type
of "BDB". 

Then double click the "default" virtual node to open up its associated tab.

In the Virtual Host tab, scroll down to "Exchanges" and "Queues".

![ex-and-qs](exchanges_and_queues.PNG "Exchanges and Queues")

Make sure to add the following exchanges with just name filled out:

1. `my-exchange`

Then add the following queues and bindings:

1. `some.queue` 

After creating the queue, double click it to open the queue's tab.
Then add the binding of some.queue to my-exchange.


