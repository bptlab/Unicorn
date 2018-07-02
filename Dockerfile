FROM tomcat:8.5.11-jre8

RUN apt-get update && apt-get install -y gettext-base && \
    cp /usr/local/tomcat/conf/server.xml /usr/local/tomcat/conf/server-template.xml

COPY EapWebInterface/target/Unicorn.war /Unicorn.war
COPY unicorn_template.properties /usr/local/tomcat/conf/unicorn_template.properties
COPY scripts/docker-start.sh /usr/local/bin/docker-start.sh
RUN chmod +x /usr/local/bin/docker-start.sh

ENTRYPOINT ["/usr/local/bin/docker-start.sh"]
