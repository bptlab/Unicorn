#!/bin/bash

if [ -z ${UNICORN_DEPLOY_NAME+x} ]; then
    export UNICORN_DEPLOY_NAME=Unicorn
fi

if [ -z ${UNICORN_DB_PORT+x} ]; then
    export UNICORN_DB_PORT=3306
fi

if [ -z ${UNICORN_DB_DEV_DB+x} ]; then
    export UNICORN_DB_DEV_DB=eap_development
fi

if [ -z ${UNICORN_DB_TEST_DB+x} ]; then
    export UNICORN_DB_TEST_DB=eap_testing
fi

cp /Unicorn.war /usr/local/tomcat/webapps/$UNICORN_DEPLOY_NAME.war
envsubst < /usr/local/tomcat/conf/unicorn.properties.tpl > /usr/local/tomcat/conf/unicorn.properties
# TODO why this?!
#envsubst < /usr/local/tomcat/conf/server-template.xml > /usr/local/tomcat/conf/server.xml
export CATALINA_OPTS="-Ddb.host=$UNICORN_DB_HOST -Ddb.port=$UNICORN_DB_PORT -Ddb.user=$UNICORN_DB_USER -Ddb.password=$UNICORN_DB_PASSWORD"
catalina.sh run
