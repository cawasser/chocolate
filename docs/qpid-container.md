## Qpid C++ broker

The qpid C++ broker is different in that it uses AMQP version 1.0, as opposed to RabbitMQ who uses 9.1.

This difference requires a separate library to perform the basic AMQP operations. This means you'll need
to have a separate broker available to test the AMQP 1.0 libraries.

### Pull a docker image

To pull a qpid broker, run

```
docker pull chadarm/qpid-broker
```

to add the docker image to your docker images. Now you can run:
`docker images` to test you still have the image.

### Running a docker image

```
docker container run -p 1111:5672 -d --name qpid CONTAINER_NAME
```

This maps the port 1111 of the machine running the container with the port 5672 within the container.
This also creates a running container from the docker image that was pulled.

Check if the container is running with `docker ps`.

### Operating within the container

```
docker exec -it qpid bash
```

#### Adding queues

```
qpid-config add queue QUEUE_NAME -b localhost:5672
```

### Start the docker image

```
docker start qpid
```


### Stopping the docker image

```
docker stop qpid
```