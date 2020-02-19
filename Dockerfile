FROM openjdk:8-alpine

WORKDIR /chocolate/

COPY target/uberjar/chocolate.jar app.jar

EXPOSE 3000

ENV DATABASE_URL="jdbc:sqlite:./chocolate_dev.db"

COPY chocolate_dev.db chocolate_dev.db

CMD ["java", "-jar", "app.jar"]