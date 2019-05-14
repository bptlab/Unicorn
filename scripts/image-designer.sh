#!/bin/bash

# Update base system
apt-get update
#apt-get --yes upgrade

# Install needed Software
apt-get --yes install curl 
apt-get --yes install python3 
apt-get --yes install maven
apt-get --yes install openjdk-8-jre icedtea-8-plugin openjdk-8-jdk openjdk-8-jre-headless 
apt-get --yes autoclean

# Set OpenJDK 8
update-java-alternatives --set java-1.8.0-openjdk-amd64