import json
from random import randint

class DockerProperties:
    @staticmethod
    def simplePassword(length):
        password = ""
        for i in range(0, length):
            # choose integer N that 65<=N<=122 and find related ascii char C that C in {A,..,z}
            password += chr(randint(65, 122))
        return password
    
    def __init__(self, filePath):
        self.filePath = filePath
        self.properties = None

    def createNormalPropertiesFile(self):
        file_object = open(self.filePath, "w")

        properties_object = {
            "mysql": {
                "user":"root",
                "password":"sqlpassword"
            },
            "tomcat": {
                "id":"localDevTomcat",
                "user":"tomcatadmin",
                "password":"tomcatpassword",
                "roles":['manager-gui', 'admin-gui', 'manager-script']
            }
        }

        json.dump(properties_object, file_object, indent=4)
        file_object.close()

    def loadProperties(self):
        file_object = open(self.filePath, "r")
        self.properties = json.load(file_object)
        file_object.close
    
    def updateProperties(self):
        file_object = open(self.filePath, "w")
        json.dump(self.properties, file_object, indent=4)
    
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

    # replace passwords by randoms if default
    def replaceSecure(self):
        password_length = 8
        changed = False
        if self.getMySQLPassword == "sqlpassword":
            changed = True
            self.setMYSQLPassword(DockerProperties.simplePassword(password_length))
        if self.getTomcatPassword == "tomcatpassword":
            changed = True
            self.setTomcatPassword(DockerProperties.simplePassword(password_length))
        if changed:
            self.updateProperties()