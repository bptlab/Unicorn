import argparse, narwhalvalues

class UnicornProperties:
    defaultFilePath = "unicorn.properties"
    defaultTemplatePath = "template.properties"

    @staticmethod
    def collectAttributes():
        properties = narwhalvalues.NarwhalProperties()
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
    
    def __init__(self, propertiesFile, templateFile):
        self.filePath = propertiesFile
        self.templatePath = templateFile
        self.content = self.readProperties()
        self.substituteContent()
        # creates properties file if not exists
        self.updateFile()
    
    def readProperties(self):
        fileHandler = open(self.templatePath, "r")
        content = ""
        for line in fileHandler:
            content += line
        fileHandler.close()
        return content

    def substituteContent(self):
        attributeCollection = UnicornProperties.collectAttributes()
        for key in attributeCollection.keys():
            self.content = self.content.replace(key, attributeCollection[key])
    
    def updateFile(self):
        fileHandler = open(self.filePath, "w")
        fileHandler.write(self.content)
        fileHandler.close()

def main():
    cmdArgsHandler = argparse.ArgumentParser(description="Adapt unicorn.properties for Narwhal")
    cmdArgsHandler.add_argument(
        "--template_path", 
        "-s",
        type=str, 
        default=UnicornProperties.defaultTemplatePath, 
        help="Reference to template file")
    cmdArgsHandler.add_argument(
        "--properties_path", 
        "-d",
        type=str,
        default=UnicornProperties.defaultFilePath,
        help="Reference to unicorn.properties")
    parsedArgs = cmdArgsHandler.parse_args()
    # process starts when object initialized
    UnicornProperties(parsedArgs.properties_path, parsedArgs.template_path)

if __name__ == "__main__":
    main()