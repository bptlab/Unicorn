#!/bin/sh
export CATALINA_OPTS="-Ddb.host=${MYSQL_PORT_3306_TCP_ADDR} -Ddb.port=${MYSQL_PORT_3306_TCP_PORT}"
catalina.sh run
