FROM pgvector/pgvector:pg14

ENV POSTGRES_PASSWORD testPassword

COPY ./ /docker-entrypoint-initdb.d/

EXPOSE 5432