#!/bin/sh

# starts in /home
# external volumes: /home/configs

mv ./web-template.xml ./configs/web-template.xml

# generate all configurations
echo "Generate narwhalvalues from environment..."
python3 narwhalvalues.py -s 1 -f configs/
echo "Generate unicorn.properties"
python3 unicornproperties.py -n configs/ -d configs/
echo "Generate tomcat config files"
python3 tomcatproperties.py -p configs/

echo "Coordinator finished"