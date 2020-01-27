FROM openjdk:8-alpine

COPY target/uberjar/chocolate.jar /chocolate/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/chocolate/app.jar"]
