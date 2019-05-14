# Installation-Notes for Linux machines

Detailed description to install Unicorn on a Linux machine. 

## Pre-requisites
To build and run Unicorn you need the following software:
- JDK 8
- Maven 3
- Apache Tomcat 7.x (or some other container)
- MySQL server 5.6 or above

## Preparation

### Download and config JDK 8
1. Download JDK 8 

2. Unzip downloaded archive 
```sh
tar -xzf archive.tar.gz
```

3. Copy folder with root privileges to _/opt/Oracle_Java/_

4. Install new Java environment, replace VERSION placeholder with correct name of your downloaded version
```sh 
sudo update-alternatives --install "/usr/bin/java" "java" "/opt/Oracle_Java/jdk1.8.0_VERSION/bin/java" 1 
```
```sh
sudo update-alternatives --install "/usr/bin/javac" "javac" "/opt/Oracle_Java/jdk1.8.0_VERSION/bin/javac" 1
```
```sh
sudo update-alternatives --install "/usr/bin/javaws" "javaws" "/opt/Oracle_Java/jdk1.8.0_VERSION/bin/javaws" 1
```
```sh
sudo update-alternatives --install "/usr/bin/jar" "jar" "/opt/Oracle_Java/jdk1.8.0_VERSION/bin/jar" 1 
``` 

5. Select rigth environment
```sh 
sudo update-alternatives --config java
```


### Download and config Tomcat 7.x
1. Download Tomcat 7.x
```sh
wget https://www-us.apache.org/dist/tomcat/tomcat-7/v7.0.93/bin/apache-tomcat-7.0.93.tar.gz
```
2. Unzip downloaded archive
```sh
tar -xzf archive.tar.gz
```
3. Rename unpacked folder to _Tomcat7_
4. Move folder with Root-Privileges to _/usr/local/_
5. Add one user profile. For that purpose edit the _/usr/local/Tomcat7/conf/tomcat-users.xml_ like it's shown below and replace _username_ and _password_ individualy
```xml 
<?xml version='1.0' encoding='utf-8'?>

<tomcat-users>
    <role rolename='manager-gui'/>
    <role rolename='admin-gui'/>
    <role rolename='manager-script'/>

    <user username='username' password='password' roles='manager-gui,admin-gui,manager-script'/>
</tomcat-users>
```

6. Change size limitations of webapps by editing the file _/usr/local/Tomcat7/webapps/manager/WEB-INF/web.xml_ and do some changes at place of `` <multipart-config> `` to allow webapps sized up to 150MB
```xml 
<multipart-config>
<max-file-size> 152428800 </max-file-size>
<max-request-size> 152428800 </max-request-size>
</multipart-config>
```
7. Start Tomcat
```sh
cd /usr/local/Tomcat7/bin
./startup.sh
````
Note: To stop the Tomcat Server do following:
```sh
cd /usr/local/Tomcat7/bin
./shutdown.sh
```

### Download and config Maven
1. Download and install Maven
```sh 
sudo apt-get install maven
```
2. Add necessary configurations to allow deployment to Tomcat-Server 
```sh
cd /usr/share/maven/conf/
sudo nano settings.xml
```
Add the following configuration at right place and replace _username_ and _password_ by your individual settings defined in Step 5 of the Tomcat configuration instructions
```xml
<servers>
    <server>
        <id>localDevTomcat</id>
        <username>username<username>
        <password>password</password>
    </server>
</servers>
```

### Download and config MySQL
1. Download necessary packages
```sh 
sudo apt-get install mysql-server
```
2. Set a password for user root
3. Add schemas _eap_testing_ and _eap_development_ in your MySQL-Database
4. If not already done, start mysql service
```sh
sudo service mysql start
```

## Install Unicorn
1. Clone the repository
3. Copy the file unicorn_template.properties, rename it to unicorn.properties, configure your database credentials and edit following plugin in _/EapWebInterface/pom.xml_
```xml
<plugin>
    <groupId>org.apache.tomcat.maven</groupId>
    <artifactId>tomcat7-maven-plugin</artifactId>
    <version>2.2</version>
    <configuration>
        <server>localDevTomcat</server>
        <url>http://localhost:8080/manager/text</url>
        <path>/Unicorn</path>
    </configuration>
</plugin>
```

4. Take care that JDK 8 is still selected

5. Build Unicorn by executing ```mvn install -DskipTests```

### Deployment to Tomcat

1. Copy _unicorn.properties_ to Tomcats configuration
```sh
sudo cp unicorn.properties /usr/local/Tomcat7/
```
2. Execute ```mvn tomcat7:deploy``` in the EapWebInterface folder to use maven for deployment

