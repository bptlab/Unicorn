import json
import argparse
from random import randint

class DockerProperties:
    defaultTomcatPass = "tomcatpassword"
    defaultMySQLPass = "sqlpassword"

    templatePath = "dockerproperties_template.json"

    @staticmethod
    def simplePassword(length):
        password = ""
        while length > 0:
            # choose integer N that 97<=N<=122 and find related ascii char C that C in {a,..,z}
            password += chr(randint(97, 122))
            length -= 1
        return password
    
    def __init__(self, filePath="dockerproperties.json"):
        self.filePath = filePath
        self.properties = None

    def loadProperties(self):
        file_object = open(self.filePath, "r")
        self.properties = json.load(file_object)
        file_object.close

    def saveProperties(self):
        file_object = open(self.filePath, "w")
        json.dump(self.properties, file_object, indent=4)
        file_object.close()

    def createNormalPropertiesFile(self):
        self.properties = {
            "mysql": {
                "user":"root",
                "password":"",
                "linkname":"databaselink"
            },
            "tomcat": {
                "id":"localDevTomcat",
                "user":"tomcatadmin",
                "password":"",
                "roles":['manager-gui', 'admin-gui', 'manager-script']
            },
            "dbfitter":{
                "timeout":10,
                "retries":20
            }
        }
        self.setMYSQLPassword(DockerProperties.defaultMySQLPass)
        self.setTomcatPassword(DockerProperties.defaultTomcatPass)
        
    def getComponent(self, componentName):
        return self.properties[componentName]

    # getters and setters for MySQL configuration
    
    def getMySQLInstace(self):
        return self.getComponent("mysql")
    
    def getMySQLUser(self):
        return self.getMySQLInstace()["user"]
    
    def getMySQLPassword(self):
        return self.getMySQLInstace()["password"]
    
    def setMYSQLPassword(self, new_password):
        self.getMySQLInstace()["password"] = new_password

    def getMySQLLink(self):
        return self.getMySQLInstace()["linkname"]

    # getters and setters for Tomcat configuration

    def getTomcatInstace(self):
        return self.getComponent("tomcat")
    
    def getTomcatID(self):
        return self.getTomcatInstace()["id"]
    
    def getTomcatUserRoles(self):
        return self.getTomcatInstace()["roles"]
    
    def getTomcatUser(self):
        return self.getTomcatInstace()["user"]
    
    def getTomcatPassword(self):
        return self.getTomcatInstace()["password"]

    def setTomcatPassword(self, new_password):
        self.getTomcatInstace()["password"] = new_password 

    # getters and setters for dbfitter configuration
    def getDbFitterInstance(self):
        return self.getComponent("dbfitter")

    def getDbFitterTimeout(self):
        return self.getDbFitterInstance()["timeout"]
    
    def getDbFitterRetries(self):
        return self.getDbFitterInstance()["retries"]

    # replace passwords by randoms if default
    def replaceSecure(self):
        password_length = 8
        changed = False
        if self.getMySQLPassword() == DockerProperties.defaultMySQLPass:
            changed = True
            self.setMYSQLPassword(DockerProperties.simplePassword(password_length))
        if self.getTomcatPassword() == DockerProperties.defaultTomcatPass:
            changed = True
            self.setTomcatPassword(DockerProperties.simplePassword(password_length))
        if changed:
            self.saveProperties()

def main():
    argumentparser = argparse.ArgumentParser(description="Script to manage dockerproperties.json file")
    argumentparser.add_argument("--mysqlpassword", "-mp", type=str, default="", help="Set password for MySQL")
    argumentparser.add_argument("--tomcatpassword", "-tp", type=str, default="", help="Set password for Tomcat" )
    argumentparser.add_argument("--randomsecurity", "-rs", type=int, default=0 ,help="Decide about default passwords")
    argumentparser.add_argument("--createtemplate", "-ct", type=int, default=0, help="Create new properties template and ignores all other options")

    argumentList = argumentparser.parse_args()

    propertiesManager = None
    if argumentList.createtemplate:
        propertiesManager = DockerProperties(DockerProperties.templatePath)
        propertiesManager.createNormalPropertiesFile()
        propertiesManager.saveProperties()
        return
    else:
        propertiesManager = DockerProperties()
        propertiesManager.createNormalPropertiesFile()
        if argumentList.mysqlpassword != "":
            propertiesManager.setMYSQLPassword(argumentList.mysqlpassword)
        if argumentList.tomcatpassword != "":
            propertiesManager.setTomcatPassword(argumentList.tomcatpassword)
        if argumentList.randomsecurity:
            propertiesManager.replaceSecure()
        propertiesManager.saveProperties()

if __name__ == "__main__":
    main()