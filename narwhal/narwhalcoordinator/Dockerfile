FROM alpine:latest

ENV DBPASSWD sqlpassword
ENV TOMCATUSER tomcatadmin
ENV TOMCATPASSWD tomcatpassword

WORKDIR /home
COPY ["./*", "./"]

RUN apk add --no-cache python3 && \
    rm Dockerfile

CMD ["sh", "coordinator.sh"]