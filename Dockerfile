FROM openjdk:8-alpine

WORKDIR /chocolate/

COPY target/uberjar/chocolate.jar app.jar

EXPOSE 3000

ENV DATABASE_URL="jdbc:sqlite:./chocolate_dev.db"

ENV RABBIT_HOST="127.0.0.1"
ENV RABBIT_PORT=5672
ENV RABBIT_USERNAME="guest"
ENV RABBIT_PASSWORD="guest"
ENV RABBIT_VHOST="/main"

COPY chocolate_dev.db chocolate_dev.db

CMD ["java", "-jar", "app.jar"]