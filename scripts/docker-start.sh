#!/bin/sh
export CATALINA_OPTS="-Ddb.host=database -Ddb.port=3306 -Ddb.user=${MYSQL_USER} -Ddb.password=${MYSQL_ROOT_PASSWORD}"
catalina.sh run
