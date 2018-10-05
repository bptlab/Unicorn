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
# let's do this to make unicorn happy
envsubst < /usr/local/tomcat/conf/unicorn.properties.tpl > /usr/local/tomcat/conf/unicorn.properties
# but db connection data is actually taken from here
export CATALINA_OPTS="-Ddb.host=$UNICORN_DB_HOST -Ddb.port=$UNICORN_DB_PORT -Ddb.dev.name=$UNICORN_DB_DEV_DB -Ddb.user=$UNICORN_DB_USER -Ddb.password=$UNICORN_DB_PASSWORD"
catalina.sh run

