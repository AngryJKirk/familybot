FROM postgres:latest

ENV POSTGRES_PASSWORD testPassword

COPY ./ /docker-entrypoint-initdb.d/

EXPOSE 5432