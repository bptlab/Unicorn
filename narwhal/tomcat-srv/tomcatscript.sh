#!/bin/sh

mkdir /home/conf

# wait until all config files and the war container exists
echo "Wait for config files..."
while [ ! -f "/home/configs/web.xml" ] || [ ! -f "/home/configs/tomcat-users.xml"] || [ ! -f "/home/configs/Unicorn.war" ]
do
    sleep 1s
done
echo "Config files found!"

# push config files & the war container to their place
mv /home/configs/web.xml /usr/local/tomcat/webapps/manager/WEB-INF/web.xml
mv /home/configs/tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml
cp /home/configs/unicorn.properties /home/conf/unicorn.properties
cp /home/configs/Unicorn.war /usr/local/tomcat/webapps/Unicorn.war

# start Tomcat server like original container CMD
/usr/local/tomcat/bin/catalina.sh run