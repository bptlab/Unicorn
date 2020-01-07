# Unicorn
An event processing platform build on top of [Esper](http://espertech.com/products/esper.php). It was developed by the [Business Process Technology group](http://bpt.hpi.uni-potsdam.de) at the [Hasso-Plattner-Institut](http://hpi.de) in the course of several student projects.

## Pre-requisites 
To build and run Unicorn you need the following software:
- Maven 3
- Apache Tomcat 7.x (or some other container)
- MySQL server 5.6 or above

## Installation

1. clone the repository
2. create schemas 'eap_development' and 'eap_testing' in your MySQL database
3. copy the file unicorn_template.properties, rename it to unicorn.properties and configure your database credentials. It needs to stay in the main directory.
4. build Unicorn by executing ```mvn install -DskipTests```

### Deployment to Tomcat

You can manually deploy Unicorn by copying the resulting war file in EapWebInterface/target to the webapps folder of your tomcat installation or you can use ```mvn tomcat7:deploy``` in the EapWebInterface folder if you want to use maven for deployment. However, some configuration is necessary for the latter option, see for example [here](http://www.mkyong.com/maven/how-to-deploy-maven-based-war-file-to-tomcat/).

**Unicorn looks for the unicorn.properties in two locations: 1) in the parent folder of user.dir (NOT user.home) and 2) in user.dir/conf. `user.dir` refers to the directory in which tomcat (and hence, Java) is started. This means you need to start tomcat from EapWebInterface. Assuming you are in the main directory of Unicorn, you can do the following.**

    cd EapWebInterface
    /path/to/your/tomcat/bin/catalina.sh run

**If your tomcat is started as a windows service, you will need to find out what user.dir is in that case. If Unicorn fails to start, check in `/path/to/your/tomcat/logs/catalina.out` for an error message 'unicorn.properties not found'.**

## Getting Started

You can now start your browser and visit the Unicorn platform running on your tomcat application server, per default it is deployed to http://localhost:8080/Unicorn
