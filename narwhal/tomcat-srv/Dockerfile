FROM tomcat:7-jdk8
WORKDIR /home
COPY ["tomcatscript.sh", "./"]
RUN chmod +x tomcatscript.sh
EXPOSE 8080
CMD ["/home/tomcatscript.sh", "run"]