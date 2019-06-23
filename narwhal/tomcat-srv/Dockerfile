FROM tomcat:9-jdk8
WORKDIR /home
COPY ["tomcatscript.sh", "./"]
RUN chmod +x tomcatscript.sh
EXPOSE 8080
CMD ["tomcatscript.sh", "run"]