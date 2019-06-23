#!/bin/sh

cd /home/configs

# get original web.xml as template for narwhalcoordinator
cp /usr/local/tomcat/webapps/manager/WEB-INF/web.xml ./web-template.xml

# wait until narwhalcoordinator generates them
while [ ! -f "./web.xml" ]
do
    sleep 1s
done

while [ ! -f "./tomcat-users.xml" ]
do
    sleep 1s
done

sleep 0.5s

# push config files to their place
cp ./web.xml /usr/local/tomcat/webapps/manager/WEB-INF/web.xml
cp ./tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml

# start Tomcat server like original container CMD
./usr/local/tomcat/bin/catalina.sh