FROM tomcat:8.5.11-jre8

COPY EapWebInterface/target/Unicorn.war /usr/local/tomcat/webapps/Unicorn.war
COPY unicorn_template.properties /usr/local/tomcat/conf/unicorn.properties
COPY scripts/docker-start.sh /usr/local/bin/docker-start.sh
RUN apt-get update && apt-get install -y gettext-base && \
    chmod +x /usr/local/bin/docker-start.sh

ENTRYPOINT ["/usr/local/bin/docker-start.sh"]
