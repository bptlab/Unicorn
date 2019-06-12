import json, os, argparse
from random import randint

class NarwhalProperties:
    defaultTomcatUser = "tomcatadmin"
    defaultTomcatPass = "tomcatpassword"
    defaultMySQLPass = "sqlpassword"

    defaultFilePath = "dockerproperties.json"

    @staticmethod
    def simplePassword(length):
        password = ""
        while length > 0:
            # choose integer N that 97<=N<=122 and find related ascii char C that C in {a,..,z}
            password += chr(randint(97, 122))
            length -= 1
        return password
    
    # interface to environment variables
    @staticmethod
    def getEnvSQLPass():
        return os.getenv("DBPASSWD", NarwhalProperties.defaultMySQLPass)

    @staticmethod
    def getEnvTomcatUser():
        return os.getenv("TOMCATUSER", NarwhalProperties.defaultTomcatUser)

    @staticmethod
    def getEnvTomcatPass():
        return os.getenv("TOMCATPASSWD", NarwhalProperties.defaultTomcatPass)
    
    def __init__(self, fileRef=defaultFilePath):
        self.filePath = fileRef
        self.properties = None
        self.createFile()

    def generateStandardStructure(self):
        self.properties = {
            "common":{
                "workingdirectory":""
            },
            "mysql": {
                "user":"root",
                "password":"",
                "link":"databaselink"
            },
            "tomcat": {
                "id":"localDevTomcat",
                "user":"tomcatadmin",
                "password":"",
                "roles":['manager-gui', 'admin-gui', 'manager-script'],
                "link":"tomcatlink"
            },
        }
        self.setMYSQLPassword(NarwhalProperties.defaultMySQLPass)
        self.setTomcatPassword(NarwhalProperties.defaultTomcatPass)
    
    def loadProperties(self):
        file_object = open(self.filePath, "r")
        self.properties = json.load(file_object)
        file_object.close

    def saveProperties(self):
        file_object = open(self.filePath, "w")
        json.dump(self.properties, file_object, indent=4)
        file_object.close()

    def createFile(self):
        try:
            fileHandler = open(self.filePath, "r")
        except FileNotFoundError:
            fileHandler = open(self.filePath, "w")
        finally:
            fileHandler.close()

    def introduceEnvVars(self):
        self.setMYSQLPassword(NarwhalProperties.getEnvSQLPass())
        self.setTomcatUser(NarwhalProperties.getEnvTomcatUser())
        self.setTomcatPassword(NarwhalProperties.getEnvTomcatPass())
        
    def getComponent(self, componentName):
        return self.properties[componentName]

    # getters and setters for MySQL configuration
    
    def getMySQLInstance(self):
        return self.getComponent("mysql")
    
    def getMySQLUser(self):
        return self.getMySQLInstance()["user"]
    
    def getMySQLPassword(self):
        return self.getMySQLInstance()["password"]
    
    def setMYSQLPassword(self, new_password):
        self.getMySQLInstance()["password"] = new_password

    def getMySQLLink(self):
        return self.getMySQLInstance()["link"]

    # getters and setters for Tomcat configuration

    def getTomcatInstance(self):
        return self.getComponent("tomcat")
    
    def getTomcatID(self):
        return self.getTomcatInstance()["id"]
    
    def getTomcatLink(self):
        return self.getTomcatInstance()["link"]

    def getTomcatUserRoles(self):
        return self.getTomcatInstance()["roles"]
    
    def getTomcatUser(self):
        return self.getTomcatInstance()["user"]

    def setTomcatUser(self, new_username):
        self.getTomcatInstance()["user"] = new_username
    
    def getTomcatPassword(self):
        return self.getTomcatInstance()["password"]

    def setTomcatPassword(self, new_password):
        self.getTomcatInstance()["password"] = new_password 

    # getters and setters for common attributes

    def getCommonInstance(self):
        return self.getComponent("common")
    def getWorkDir(self):
        return self.getCommonInstance()["workingdirectory"]
    def setWorkDir(self, new_directory):
        self.getCommonInstance()["workingdirectory"] = new_directory
    
    # replace passwords by randoms if default
    def replaceSecure(self):
        password_length = 8
        changed = False
        if self.getMySQLPassword() == NarwhalProperties.defaultMySQLPass:
            changed = True
            self.setMYSQLPassword(NarwhalProperties.simplePassword(password_length))
        if self.getTomcatPassword() == NarwhalProperties.defaultTomcatPass:
            changed = True
            self.setTomcatPassword(NarwhalProperties.simplePassword(password_length))
        if changed:
            self.saveProperties()

def main():
    cmdArgsHandler = argparse.ArgumentParser(description="Module to organise attributes for Narwhal")
    cmdArgsHandler.add_argument("-s", "--secureMode", type=int, default=0, help="Avoid default passphrases. Default 0")
    cmdArgsHandler.add_argument("-f", "--filePath", type=str, default=NarwhalProperties.defaultFilePath, help="Set path for properties file.")
    parsedArgs = cmdArgsHandler.parse_args()
    propertiesManager = NarwhalProperties(parsedArgs.filePath)
    propertiesManager.generateStandardStructure()
    propertiesManager.introduceEnvVars()
    if parsedArgs.secureMode:
        propertiesManager.replaceSecure()
    propertiesManager.saveProperties()

if __name__ == "__main__":
    main()