import mysql.connector
import properties_manager

class MySQLConfigurationWorker:
    neededSchemas = ["eap_testing", "eap_development"]

    def quitConnection(self):
        self.databaseCursor = None
        self.databaseConnector.close()
    
    def createConnection(self, password):
        self.databaseConnector = mysql.connector.connect(
            host=self.propertiesManager.getMySQLLink(),
            user=self.propertiesManager.getMySQLUser,
            passwd=password
        )
        self.databaseCursor = self.databaseConnector.cursor()    
    
    def __init__(self):
        self.propertiesManager = properties_manager.DockerProperties()
        self.propertiesManager.loadProperties()
        # database objects
        self.databaseConnector = None
        self.databaseCursor = None
        self.createConnection(properties_manager.DockerProperties.defaultMySQLPass)
    
    # TODO: Debug
    def changePassword(self):
        self.databaseCursor.execute("USE mysql")
        self.databaseConnector._execute("ALTER USER {} IDENTIFIED BY {}".format(
            self.propertiesManager.getMySQLUser(),
            self.propertiesManager.getMySQLPassword()
        ))
        self.databaseCursor.execute("FLUSH PRIVILEGES")
        self.quitConnection()
        self.createConnection(self.propertiesManager.getMySQLPassword())
    
    def createDatabases(self):
        for database in MySQLConfigurationWorker.neededSchemas:
            # proof that schema not already exists
            not_exits = True
            self.databaseCursor.execute("SHOW DATABASES")
            for vartuple in self.databaseCursor:
                if vartuple[0] == database:
                    not_exits = False
            if not_exits:
                self.databaseCursor.execute("CREATE DATABASE {}".format(database))

def main():
    worker = MySQLConfigurationWorker()
    worker.changePassword()
    worker.createDatabases()
    worker.quitConnection()

if __name__ == "__main__":
    main()