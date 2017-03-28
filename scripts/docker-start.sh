#!/bin/sh
export CATALINA_OPTS="-Ddb.host=${MYSQL_PORT_3306_TCP_ADDR} -Ddb.port=${MYSQL_PORT_3306_TCP_PORT} -Ddb.user=${MYSQL_ENV_MYSQL_USER} -Ddb.password=${MYSQL_ENV_MYSQL_PASSWORD}"
export
catalina.sh run
