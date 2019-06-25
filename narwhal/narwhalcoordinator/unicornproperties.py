import argparse, narwhalvalues

class UnicornProperties:
    defaultTemplatePath = "template.properties"

    @staticmethod
    def collectAttributes(narwhalvaluesPath):
        properties = narwhalvalues.NarwhalProperties(narwhalvaluesPath)
        properties.loadProperties()
        keyValuePairs = {
            "$workdir$": properties.getWorkDir(),
            "$dblink$": properties.getMySQLLink(),
            "$dbusername$": properties.getMySQLUser(),
            "$dbpassword$": properties.getMySQLPassword(),
            "$tomcatid$": properties.getTomcatID(),
            "$tomcatlink$": properties.getTomcatLink()
        }
        return keyValuePairs
    
    def __init__(self, narwhalvaluesPath, resultPath):
        self.propertiesPath = narwhalvaluesPath 
        self.resultPath = resultPath + "unicorn.properties"
        self.content = self.readProperties()
        self.substituteContent()
        # creates properties file if not exists
        self.updateFile()
    
    # read initial template of unicorn.properties
    def readProperties(self):
        fileHandler = open(UnicornProperties.defaultTemplatePath, "r")
        content = ""
        for line in fileHandler:
            content += line
        fileHandler.close()
        return content

    def substituteContent(self):
        attributeCollection = UnicornProperties.collectAttributes(self.propertiesPath)
        for key in attributeCollection.keys():
            self.content = self.content.replace(key, attributeCollection[key])
    
    def updateFile(self):
        fileHandler = open(self.resultPath, "w")
        fileHandler.write(self.content)
        fileHandler.close()

def main():
    cmdArgsHandler = argparse.ArgumentParser(description="Adapt unicorn.properties for Narwhal")
    cmdArgsHandler.add_argument(
        "--narhwahlValPath", 
        "-n",
        type=str, 
        default="", 
        help="Reference to narwhal properties file")
    cmdArgsHandler.add_argument(
        "--destinationPath", 
        "-d",
        type=str,
        default="",
        help="Reference to final unicorn.properties")
    parsedArgs = cmdArgsHandler.parse_args()
    # process starts when object initialized
    UnicornProperties(parsedArgs.narhwahlValPath, parsedArgs.destinationPath)

if __name__ == "__main__":
    main()