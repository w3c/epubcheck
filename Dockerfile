# build the epubcheck.jar file
FROM maven:slim AS builder

WORKDIR /app
COPY . .
RUN mvn clean install

# prepare runner for epubcheck.jar execution
FROM amazoncorretto:25.0.1-alpine3.22

WORKDIR /app
COPY --from=builder /app .
RUN echo -e '#!/bin/sh\njava -jar /app/target/epubcheck.jar "${@}"\n' > entrypoint.sh
RUN chmod +x entrypoint.sh

ENV DATA_PATH=/data
WORKDIR ${DATA_PATH}
VOLUME ${DATA_PATH}

ENTRYPOINT [ "/app/entrypoint.sh" ]
