import json

class DockerProperties:
    def __init__(self):
        self.filePath = "Unicorn/dockerproperties.json"
        self.properties = None

    def createNormalPropertiesFile(self):
        file_object = open(self.filePath, "w")

        properties_object = {
            "tomcat": {
                "id":"localDevTomcat",
                "userprofile": {
                    "user":"tomcatadmin",
                    "password":"adminpassword",
                    "roles": ['manager-gui', 'admin-gui', 'manager-script']
                } 
            }
        }

        json.dump(properties_object, file_object, indent=4)
        file_object.close()

    def loadProperties(self):
        file_object = open(self.filePath, "r")
        self.properties = json.load(file_object)
        file_object.close
    
    def getTomcatID(self):
        return self.properties["tomcat"]["id"]

    def getUserProfile(self):
        return self.properties["tomcat"]["userprofile"]
    
    def getRoles(self):
        return self.getUserProfile()["roles"]
    
    def getUserName(self):
        return self.getUserProfile()["user"]
    
    def getPassword(self):
        return self.getUserProfile()["password"]