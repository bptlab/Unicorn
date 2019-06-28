#!/bin/bash

# starts at /home
# external volumes: /home/confgis & /home/source

# if already builded, do nothing
if [ -f "/home/configs/Unicorn.war" ] 
then exit 0
fi

# wait until raise flag that all config files are prepared
while [ ! -f "/home/configs/unicorn.properties" ]
do
    sleep 1s
done

mkdir workdirectory
cp -r /home/source/* /home/workdirectory/
cp /home/configs/unicorn.properties /home/workdirectory

cd /home/workdirectory
mvn install -DskipTests
cp /home/workdirectory/EapWebInterface/target/Unicorn.war /home/configs/Unicorn.war