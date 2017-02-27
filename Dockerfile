FROM tomcat:8.5.11-jre8

COPY EapWebInterface/target/Unicorn.war /usr/local/tomcat/webapps/Unicorn.war
COPY unicorn_template.properties /usr/local/tomcat/conf/unicorn.properties
COPY docker-start.sh /usr/local/bin/docker-start.sh

ENTRYPOINT ["docker-start.sh"]