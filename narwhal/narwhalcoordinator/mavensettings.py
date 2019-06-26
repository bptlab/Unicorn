import narwhalvalues, argparse
import xml.etree.ElementTree as xmlWorker

class NarwhalMavenSettings:
    settingsTags = [
        "pluginGroups",
        "proxies",
        "servers",
        "mirrors",
        "profiles"
    ]

    @staticmethod
    def collectAttributes(narwhalValPath):
        propertiesHandler = narwhalvalues.NarwhalProperties(narwhalValPath)
        propertiesHandler.loadProperties()
        keyValuePairs = {
            "id" : propertiesHandler.getTomcatID(),
            "username" : propertiesHandler.getTomcatUser(),
            "password" : propertiesHandler.getTomcatPassword()
        }
        return keyValuePairs

    def __init__(self, narwhalvalues, destination):
        self.narwhalValPath = narwhalvalues
        self.destinationpath = destination
        self.settingsStructure = None
        self.generateStructure(NarwhalMavenSettings.collectAttributes(self.narwhalValPath))

    def generateStructure(self, serverconfig):
        self.settingsStructure = xmlWorker.Element("settings")
        for subtag in NarwhalMavenSettings.settingsTags:
            tmp = xmlWorker.SubElement(self.settingsStructure, subtag)
            if subtag == "servers":
                for serverKey in serverconfig:
                    xmlWorker.SubElement(tmp, serverKey).text = serverconfig[serverKey]
            else:
                tmp.append(xmlWorker.Comment(
                    "Statements for {} configs".format(subtag)
                ))

    def writeStructureToFile(self):
        output = xmlWorker.tostring(self.settingsStructure, encoding='utf8').decode('utf8')
        fileHanlder = open(self.destinationpath + "settings.xml", "w")
        fileHanlder.write(output)
        fileHanlder.close()

def main():
    cmdArgsHandler = argparse.ArgumentParser(description="Module to adapt mavens settings.xml")
    cmdArgsHandler.add_argument("-n", "--narwhalvaluespath", type=str, default="", help="Path to find the narwhalproperties.json")
    cmdArgsHandler.add_argument("-d", "--destinationpath", type=str, default="", help="Path to store the final settings.xml")
    parsedArgs = cmdArgsHandler.parse_args()
    mavenSettingsManager = NarwhalMavenSettings(
        parsedArgs.narwhalvaluespath,
        parsedArgs.destinationpath
    )
    mavenSettingsManager.writeStructureToFile()

if __name__ == "__main__":
    main()