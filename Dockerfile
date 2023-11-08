FROM ghcr.io/navikt/baseimages/temurin:17

COPY build/libs/omsorgsopptjening-bestem-pensjonsopptjening.jar /app/app.jar

ARG JAVA_OPTS=-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/
