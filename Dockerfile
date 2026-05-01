# build the epubcheck.jar file
FROM maven:3-eclipse-temurin-25-alpine AS builder

WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

# prepare runner for epubcheck.jar execution
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app
COPY --from=builder /app/target/epubcheck.jar /app/epubcheck.jar
COPY --from=builder /app/target/lib /app/lib
RUN printf '#!/bin/sh\njava -jar /app/epubcheck.jar "$@"\n' > /app/entrypoint.sh
RUN chmod +x entrypoint.sh

ENV DATA_PATH=/data
WORKDIR ${DATA_PATH}
VOLUME ${DATA_PATH}

ENTRYPOINT [ "/app/entrypoint.sh" ]
