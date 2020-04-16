# build the epubcheck.jar file
FROM maven:slim as builder

WORKDIR /app
COPY . .
RUN mvn clean install

# prepare runner for epubcheck.jar execution
FROM openjdk:slim

WORKDIR /app
COPY --from=builder /app .
RUN echo '#!/bin/bash\n java -jar /app/target/epubcheck.jar "${@:1}"\n' > entrypoint.sh
RUN chmod +x entrypoint.sh

ENV DATA_PATH=/data
WORKDIR ${DATA_PATH}
VOLUME ${DATA_PATH}

ENTRYPOINT [ "/app/entrypoint.sh" ]
