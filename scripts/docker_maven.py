import xml.etree.ElementTree as xmlWorker
import json
import docker_properties

propertiesHandler = docker_properties.DockerProperties()
propertiesHandler.loadProperties()
tomcatInfo = {
    "id" : propertiesHandler.getTomcatID(), 
    "username" : propertiesHandler.getUserName(), 
    "password" : propertiesHandler.getPassword()
    }

xmlWorker.register_namespace("", "http://maven.apache.org/SETTINGS/1.0.0")
configFile = xmlWorker.parse("settings.xml")
rootVertex = configFile.getroot()

pseudo_attributes = {}

for child in rootVertex:
    if child.tag == "{http://maven.apache.org/SETTINGS/1.0.0}servers":
        newServer = child.makeelement('server', pseudo_attributes)

        for tag in tomcatInfo:
            serverDescribingTag = newServer.makeelement(tag, pseudo_attributes)
            serverDescribingTag.text = tomcatInfo[tag]
            newServer.append(serverDescribingTag)
        
        child.append(newServer)

configFile.write("settings.xml")
