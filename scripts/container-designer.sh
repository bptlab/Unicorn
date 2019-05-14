#!/bin/bash

cd /home

# Get scripts
cp Unicorn/scripts/docker_maven.py .
cp Unicorn/scripts/docker_tomcat.py .
cp Unicorn/scripts/docker_properties.py .

cp Unicorn/scripts/build.sh .
cp Unicorn/scripts/deploy.sh .
cp Unicorn/scripts/undeploy.sh .
cp Unicorn/scripts/deploy_notests.sh .
chmod +x build.sh
chmod +x deploy.sh
chmod +x undeploy.sh
chmod +x deploy_notests.sh

# Proof for Tomcat installation/ install Tomcat
cd /usr/local/Tomcat7
if [ $(ls -1 | wc -l) -eq 0 ]; then
    # Download & extract
    curl --output tomcat.tar.gz ftp://ftp.fu-berlin.de/unix/www/apache/tomcat/tomcat-7/v7.0.94/bin/apache-tomcat-7.0.94.tar.gz
    mkdir TomcatExtract
    tar -xzf tomcat.tar.gz -C TomcatExtract
    rm tomcat.tar.gz
    cd TomcatExtract
    cd $(ls)
    mv * ../../
    cd ../..
    rm -r TomcatExtract
    chmod +x bin/startup.sh
fi
cd /home

# Execute scriptbased configuration for Tomcat7
touch tomcat-users.xml
cp /usr/local/Tomcat7/webapps/manager/WEB-INF/web.xml .
python3 docker_tomcat.py
mv web.xml /usr/local/Tomcat7/webapps/manager/WEB-INF/web.xml
mv tomcat-users.xml /usr/local/Tomcat7/conf/tomcat-users.xml

# Execute scriptbased configuration for Maven 
cp /usr/share/maven/conf/settings.xml .
python3 docker_maven.py
mv settings.xml /usr/share/maven/conf/settings.xml

cp /home/Unicorn/unicorn.properties /

# Remove expired scripts
rm docker_tomcat.py
rm docker_maven.py
rm docker_properties.py

/usr/local/Tomcat7/bin/startup.sh

/bin/bash