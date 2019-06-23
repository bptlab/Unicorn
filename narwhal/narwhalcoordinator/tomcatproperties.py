import narwhalvalues, argparse
import xml.etree.ElementTree as xmlWorker

class TomcatProperties:

    @staticmethod
    def rolesListToString(rolesList):
        result = ""
        for role in rolesList:
            result += role + ", "
        # remove last ", "
        return result[0:-2]
    
    def __init__(self, workdir):
        self.path = workdir
        self.userconfig = None
        self.webconfig = ""
        self.commonValuesHandler = narwhalvalues.NarwhalProperties(workdir)        
        self.commonValuesHandler.loadProperties()
    
    def generateUserConfig(self):
        # create basic structure 
        self.userconfig = xmlWorker.Element('tomcat-users')
        # add roles
        for role in self.commonValuesHandler.getTomcatUserRoles():
            xmlWorker.SubElement(self.userconfig, 'role').set('rolename', role)
        # add user with password and it's privileges
        tmp = xmlWorker.SubElement(self.userconfig, 'user')
        tmp.set('username', self.commonValuesHandler.getTomcatUser())
        tmp.set('password', self.commonValuesHandler.getTomcatPassword())
        tmp.set('roles', TomcatProperties.rolesListToString(self.commonValuesHandler.getTomcatUserRoles()))

    def writeUserConfigToFile(self):
        structure_string = xmlWorker.tostring(self.userconfig, encoding='utf8').decode('utf8')
        fileHandler = open(self.path + "tomcat-users.xml", "w")
        fileHandler.write(structure_string)
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