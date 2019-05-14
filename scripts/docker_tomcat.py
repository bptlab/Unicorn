import xml.etree.ElementTree as xmlWorker
import json
import docker_properties

# 150MB
size_const = 152428800 

listterminators = ["\'", "[", "]", " "]

tmp = None
file_handler = None

propertiesHandler = docker_properties.DockerProperties()
propertiesHandler.loadProperties()
tomcatUserRolesCaps = propertiesHandler.getRoles()
tomcatUsers = {
    propertiesHandler.getUserName() : propertiesHandler.getPassword()
}

# create the file structure
tomcatUsersStruct = xmlWorker.Element('tomcat-users') 

# add roles
for role in tomcatUserRolesCaps:
    xmlWorker.SubElement(tomcatUsersStruct, 'role').set('rolename', role)

# add users
for user in tomcatUsers:
    privileges = str(tomcatUserRolesCaps)
    for terminator in listterminators:
        privileges = privileges.replace(terminator, "")
    tmp = xmlWorker.SubElement(tomcatUsersStruct, 'user')
    tmp.set('username', user)
    tmp.set('password', tomcatUsers[user])
    tmp.set('roles', privileges)

# write structure to file
structure_string = xmlWorker.tostring(tomcatUsersStruct, encoding='utf8').decode('utf8')
file_handler = open("tomcat-users.xml", "w")
file_handler.write(structure_string)
file_handler.close()

# change server config xml as necessary
xmlWorker.register_namespace("", "http://java.sun.com/xml/ns/javaee")
webxml = xmlWorker.parse("web.xml")
webxml_root = webxml.getroot() 
for sizeField in webxml_root.iter('{http://java.sun.com/xml/ns/javaee}max-file-size'):
    sizeField.text = str(size_const)
for sizeField in webxml_root.iter('{http://java.sun.com/xml/ns/javaee}max-request-size'):
    sizeField.text = str(size_const)
webxml.write("web.xml")
