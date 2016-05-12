# Unicorn
An event processing platform build on top of [Esper](http://espertech.com/products/esper.php). It was developed by the [Business Process Technology group](http://bpt.hpi.uni-potsdam.de) at the [Hasso-Plattner-Institut](http://hpi.de) in the course of several student projects.

## Pre-requisites 
To build and run Unicorn you need the following software:
- Maven 3
- Apache Tomcat 7.x (or some other container)
- MySQL server 5.6 or above

## Installation

1. clone the repository
2. unzip jbpt.zip
3. run ```mvn install``` in folder jbpt
4. Create schemas 'eap_development' and 'eap_testing' in your Mysql DB
5. copy unicorn-template.properties to unicorn.properties and configure DB credentials
6. run ```mvn install -DskipTests``` in main folder

### Deployment to Tomcat

You can manually deploy Unicorn by copying the resulting war file in EapWebInterface/target to the webapps folder of your tomcat installation or you can use ```mvn tomcat7:deploy``` in the EapWebInterface folder if you want to use maven for deployment. However, some configuration is necessary for the latter option, see for example [here](http://www.mkyong.com/maven/how-to-deploy-maven-based-war-file-to-tomcat/).

## Getting Started

You can now start your browser and visit the Unicorn platform running on your tomcat application server, per default it is deployed to http://localhost:8080/Unicorn

### Creating event types

### Importing events


## Further Information

Please visit http://bpt.hpi.uni-potsdam.de/UNICORN for further information
