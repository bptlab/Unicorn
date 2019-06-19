import narwhalvalues, argparse

class TomcatProperties:
    user_template = """
    <?xml version="1.0" encoding="UTF-8"?>
    <tomcat-users 
        xmlns="http://tomcat.apache.org/xml"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://tomcat.apache.org/xml tomcat-users.xsd"
        version="1.0">
    <role rolename='manager-gui'/>
    <role rolename='admin-gui'/>
    <role rolename='manager-script'/>
    <user username='{}' password='{}' roles='{}'/>
    </tomcat-users> """

    @staticmethod
    def rolesListToString(rolesList):
        result = ""
        for role in rolesList:
            result += role + ", "
        # remove last ", "
        return result[0:-2]
    
    def __init__(self, workdir):
        self.path = workdir
        self.userconfig = ""
        self.webconfig = ""
        self.commonValuesHandler = narwhalvalues.NarwhalProperties()        
        self.commonValuesHandler.loadProperties()
    
    def generateUserConfig(self):
        self.userconfig = TomcatProperties.user_template.format(
            self.commonValuesHandler.getTomcatUser(),
            self.commonValuesHandler.getTomcatPassword(),
            TomcatProperties.rolesListToString(
                self.commonValuesHandler.getTomcatUserRoles()
            )
        )

    def writeUserConfigToFile(self):
        fileHandler = open(self.path + "tomcat-users.xml", "w")
        fileHandler.write(self.userconfig)
        fileHandler.close()

    def loadWebConfig(self):
        fileHandler = open(self.path + "web-template.xml", "r")
        for line in fileHandler:
            self.webconfig += line
        fileHandler.close()

    def writeWebConfigToFile(self):
        fileHandler = open(self.path + "web.xml", "w")
        fileHandler.write(self.webconfig)
        fileHandler.close()

    def adaptWebConfig(self):
        self.webconfig = self.webconfig.replace("52428800", "152428800")

    def handleJobs(self):
        self.generateUserConfig()
        self.writeUserConfigToFile()
        self.loadWebConfig()
        self.adaptWebConfig()
        self.writeWebConfigToFile()

def main():
    cmdArgsHandler = argparse.ArgumentParser(description="Module to handle configuration for Tomcat server")
    cmdArgsHandler.add_argument("--path", "-p", type=str, default="./" ,help="Path to workdirectory")
    parsedArgs = cmdArgsHandler.parse_args()
    tomcatConfigurator = TomcatProperties(parsedArgs.path)
    tomcatConfigurator.handleJobs()

if __name__ == "__main__":
    main()