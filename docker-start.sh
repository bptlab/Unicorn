#!/bin/sh

echo "Hello"
export CATALINA_OPTS="-Ddb.host=${MYSQL_PORT_3306_TCP_ADDR} -Ddb.port=${MYSQL_PORT_3306_TCP_PORT}"
echo $CATALINA_OPTS
echo "hhhh"
catalina.sh run