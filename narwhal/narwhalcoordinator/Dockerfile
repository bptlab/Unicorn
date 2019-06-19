FROM alpine:latest

ENV DBPASSWD sqlpassword
ENV TOMCATUSER tomcatadmin
ENV TOMCATPASSWD tomcatpassword

WORKDIR /home
COPY ["./*", "./"]

RUN apk add --no-cache python3 && \
    rm Dockerfile && \
    chmod +x coordinator.sh

CMD ["sh", "coordinator.sh"]