#!/bin/sh

# starts in /home

python3 narwhalvalues.py -s 1 -f configs/
# python3 unicornproperties.py

# wait until raise flag that all config files are prepared
while [ ! -f "./configs/coordinator.work" ]
do
    sleep 1s
done
# delete flag raised by tomcat
rm ./configs/coordinator.work
python3 tomcatproperties.py -p configs/
# signalize tomcat that config files are ready
touch ./configs/coordinator.finished