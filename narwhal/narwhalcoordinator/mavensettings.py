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

    def __init__(self):
        self.settingsStructure = None

    def generateStructure(self, serverconfig):
        self.settingsStructure = xmlWorker.Element("settings")
        for subtag in NarwhalMavenSettings.settingsTags:
            tmp = xmlWorker.SubElement(self.settingsStructure, subtag)
            if subtag == "servers":
                pass


def main():
    pass

if __name__ == "__main__":
    main()