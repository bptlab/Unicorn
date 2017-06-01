#!/bin/bash
envsubst < /usr/local/tomcat/conf/server.xml > /usr/local/tomcat/conf/server.xml
export CATALINA_OPTS="-Ddb.host=database -Ddb.port=3306 -Ddb.user=${MYSQL_USER} -Ddb.password=${MYSQL_ROOT_PASSWORD}"
echo CATALINA_OPTS
catalina.sh run
