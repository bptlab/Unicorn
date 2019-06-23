#!/bin/sh

cd /home/configs

# get original config files as template for narwhalcoordinator
cp /usr/local/tomcat/webapps/manager/WEB-INF/web.xml ./web-template.xml
cp /usr/local/tomcat/conf/tomcat-users.xml ./tomcat-users-template.xml

# signalize coordinator to do his job
touch coordinator.work

# wait until narwhalcoordinator signalize it's finished
while [ ! -f "./coordinator.finished" ]
do
    sleep 1s
done

# delete flag raised by coordinator
rm coordinator.finished 

# push config files to their place
cp ./web.xml /usr/local/tomcat/webapps/manager/WEB-INF/web.xml
cp ./tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml

# start Tomcat server like original container CMD
/usr/local/tomcat/bin/catalina.sh run