FROM openjdk:8-alpine

WORKDIR /chocolate/

COPY target/uberjar/chocolate.jar app.jar

EXPOSE 3000

ENV DATABASE_URL="jdbc:sqlite:./chocolate_dev.db"

ENV BROKER_HOST="localhost"
ENV BROKER_PORT=5672
ENV BROKER_USERNAME="guest"
ENV BROKER_PASSWORD="guest"
ENV BROKER_VHOST="/main"

ENV BROKER_URL_1="amqp://localhost:1111"


COPY chocolate_dev.db chocolate_dev.db

CMD ["java", "-jar", "app.jar"]