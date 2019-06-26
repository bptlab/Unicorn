FROM ubuntu:bionic

RUN apt-get update && \
    apt-get --yes install openjdk-8-jre icedtea-8-plugin openjdk-8-jdk openjdk-8-jre-headless && \
    apt-get --yes install maven && \
    apt-get --yes autoclean && \
    update-java-alternatives --set java-1.8.0-openjdk-amd64

WORKDIR /home
COPY ["./*", "./"]
RUN chmod +x mavenhandler.sh

CMD ["./mavenhandler.sh"]