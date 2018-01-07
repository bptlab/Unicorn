# UNICORN
# Copyright (c) 2012-2015 Business Process Technology (BPT), http://bpt.hpi.uni-potsdam.de.
# All Rights Reserved.
# Location of unicorn.properties
de.hpi.unicorn.projectFolder=YOUR WORKING DIRECTORY
# Database credentials
db.dev.url=jdbc:mariadb://$UNICORN_DB_HOST:$UNICORN_DB_PORT/$UNICORN_DB_DEV_DB?createDatabaseIfNotExist=true
db.test.url=jdbc:mariadb://$UNICORN_DB_HOST:$UNICORN_DB_PORT/$UNICORN_DB_TEST_DB?createDatabaseIfNotExist=true
# need to be set in catalina opts somehow
#db.user=root
#db.password=root
# Usage of historic events
# false = events are not stored in the database and correlation is disabled as it requires persisted events
de.hpi.unicorn.eventhandling.persistEvents=true
# JMS
de.hpi.unicorn.messageQueue.jmsHost=$UNICORN_JMS_HOST
de.hpi.unicorn.messageQueue.jmsPort=$UNICORN_JMS_PORT
de.hpi.unicorn.messageQueue.jmsImportChannel=$UNICORN_JMS_IMPORT_CHANNEL
# Pre-load of event types located in src/main/resources/predefinedEventTypes
de.hpi.unicorn.esper.StreamProcessingAdapter.registerPredefinedEventTypes=true
# Pre-load of transformation rules from src/main/resources/transformationRules.xml
de.hpi.unicorn.esper.StreamProcessingAdapter.registerTransformationRules=false
# Support of on-demand queries
de.hpi.unicorn.esper.supportingOnDemandQueries=false
# Handling of multiple values in XML events
# CROSS = output multiple events if attribute has multiple values (cross product, each events has one of the values)
# FIRST = output single event with first of multiple values for the affected attribute (default setting)
# LAST = output single event with last of multiple values for the affected attribute
# CONCAT = output single event, concatenate multiple values (only applicable if attribute is specified as String, otherwise CROSS applies if neither of them is given)
de.hpi.unicorn.importer.xml.multipleEventValues=FIRST
# E-mail credentials
de.hpi.unicorn.email.user=YOUR GMAIL ADDRESS HERE
de.hpi.unicorn.email.password=YOUR PASSWORD HERE
# Triplestore usage
de.hpi.unicorn.semantic.Triplestore.location=./Triplestore
de.hpi.unicorn.semantic.Triplestore.clean=true
# Default time interval for polling of events via adapter (in seconds)
de.hpi.unicorn.adapter.defaultInterval=900
# Nokia Here credentials for adapter
de.hpi.unicorn.adapter.nokiaHereAppID=YOUR HERE APP ID
de.hpi.unicorn.adapter.nokiaHereAppCode=YOUR HERE APP CODE
de.hpi.unicorn.adapter.tflAppId=YOUR HERE APP ID
de.hpi.unicorn.adapter.tflAppCode=YOUR HERE APP CODE
# Tomcat
# Make sure that the Maven server profile is defined properly in settings.xml
# Example for [path to user folder]/.m2/settings.xml:
#
# <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
#       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
#       xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
#                           http://maven.apache.org/xsd/settings-1.0.0.xsd">
#    <servers>
#        <server>
#            <id>localDevTomcat</id>
#            <username>user</username>
#            <password>pass</password>
#        </server>
#     </servers>
# </settings>
org.apache.tomcat.maven.server=localDevTomcat
org.apache.tomcat.maven.url=http://localhost:8080/manager/text
org.apache.tomcat.maven.path=/UNICORN-develop
# log4j output of Wicket UI (set OFF to disable)
log4j.logger.org.apache.wicket=INFO
log4j.logger.org.apache.wicket.protocol.http.HttpSessionStore=INFO
log4j.logger.org.apache.wicket.version=INFO
log4j.logger.org.apache.wicket.RequestCycle=INFO
#log4j output of web service (set OFF to disable)
log4j.logger.de.hpi.unicorn.EventProcessingPlatformWebservice=DEBUG,file
log4j.logger.de.hpi.unicorn.eventhandling=DEBUG,file
# everything below is log4j specific - check log4j documentation
# log4j Stdout
log4j.appender.Stdout=org.apache.log4j.ConsoleAppender
log4j.appender.Stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.Stdout.layout.conversionPattern=%-5p - %-26.26c{1} - %m\n
# Direct log messages to a log file
log4j.appender.file=org.apache.log4j.RollingFileAppender
log4j.appender.file.File=/var/lib/tomcat7/logs/wscall.log
log4j.appender.file.MaxFileSize=10MB
log4j.appender.file.MaxBackupIndex=10
log4j.appender.file.layout=org.apache.log4j.PatternLayout
log4j.appender.file.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n
log4j.rootLogger=INFO,Stdout
