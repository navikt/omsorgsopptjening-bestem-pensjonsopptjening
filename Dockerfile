FROM ghcr.io/navikt/baseimages/temurin:17

COPY build/libs/omsorgsopptjening-bestem-pensjonsopptjening.jar /app/app.jar

ENV JAVA_OPTS=-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/
