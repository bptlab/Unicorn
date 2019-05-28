# Installation Guide for Unicorn on Linux

This article describes how to install and run Unicorn on Linux machines.

__Note:__ Think about which way do you prefer to install and run Unicorn: On the one hand you directly install it on your Linux OS as mostly every other software. On the other hand, you use the way based on Docker containers. This arcticle only explains the first way! \
__Note:__ This article uses Ubuntu 18.04 to explain the installation. \
__Note:__ Take care that you are owner of sudo privileges!

## Pre-requisites

To build and run Unicorn you need the following software:

- OpenJDK 8
- Maven 3
- Apache Tomcat 7.x
- MySQL server 5.6 or above

## Preparation

### Set up OpenJDK 8

1. Install OpenJDK 8

```sh
sudo apt-get install openjdk-11-jdk openjdk-11-jre-headless
```

2. Select OpenJDK 8

```sh
sudo update-alternatives --config java
```

### Set up Tomcat 7.x

1. Download Tomcat 7.x \
 Choose a mirror on <https://tomcat.apache.org/>. Maybe you want to use wget to download the archive.

```sh
wget <mirror-link>
```

2. Unzip downloaded archive

```sh
tar -xzf archive.tar.gz
```

3. Rename unpacked folder to _Tomcat7_

4. Move folder with Root-Privileges to _/usr/local/_

5. Add necessary user profile\
Edit the _/usr/local/Tomcat7/conf/tomcat-users.xml_ like it's shown below and replace _username_ and _password_ individualy.

```xml
<?xml version='1.0' encoding='utf-8'?>

<tomcat-users>
    <role rolename='manager-gui'/>
    <role rolename='admin-gui'/>
    <role rolename='manager-script'/>

    <user username='username' password='password' roles='manager-gui,admin-gui,manager-script'/>
</tomcat-users>
```

6. Change size limitations for webapps \
Edit the file _/usr/local/Tomcat7/webapps/manager/WEB-INF/web.xml_ and do some changes as shown below at place of `` <multipart-config> `` to allow webapps sized up to 150MB.

```xml
...
<multipart-config>
<max-file-size> 152428800 </max-file-size>
<max-request-size> 152428800 </max-request-size>
</multipart-config>
...
```

7. Start Tomcat

```sh
/usr/local/Tomcat7/bin/startup.sh
````

__Note:__ Do not use sudo privileges because it is an safety issue! \
__Note:__ If you want to shutdown Tomcat, run _shutdown.sh_ instead of _startup.sh_!

### Set up Maven

1. Install Maven

```sh
sudo apt-get install maven
```

2. Add Tomcat properties to configuration

```sh
sudo nano /usr/share/maven/conf/settings.xml
```

Add the following configuration at right place as shown below and replace _username_ and _password_ by your individual settings defined in step 5 of the Tomcat setup.

```xml
...
<servers>
    <server>
        <id>localDevTomcat</id>
        <username>username<username>
        <password>password</password>
    </server>
</servers>
...
```

### Set up the MySQL-Server

1. Install MySQL-Server

```sh
sudo apt-get install mysql-server
```

2. Start MySQL-Server

```sh
sudo service mysql start
```

2. Create a password for root user

```sh
sudo mysqladmin -u root password 'password'
```

__Note:__ Replace _'password'_ by your own without quotes!

3. Add schemas _eap_testing_ and _eap_development_ in your MySQL-Database

```sh
sudo mysqladmin -u root -p create eap_testing
sudo mysqladmin -u root -p create eap_development
```

4. Restart MySQL-Server

```sh
sudo service mysql stop
sudo service mysql start
```

## Install Unicorn

1. Clone the repository

```sh
git clone https://github.com/bptlab/Unicorn.git
```

2. Set up Unicorn properties \
Copy the file unicorn_template.properties, rename it to unicorn.properties and enter your database credentials at rigth place. Move a copy to _/usr/local/Tomcat7/conf/_

4. Build Unicorn

```sh
mvn install -DskipTests
```

4. Deploy Unicorn to Tomcat

```sh
mvn tomcat7:deploy
```

__Note:__ If you run into an error, check the working directory of Tomcat by reading _/usr/local/Tomcat7/bin/catalina.out_. The file _unicorn.properties_ from step 2 has to exist in the parent directory!