FROM tomcat:9-jdk8
WORKDIR /home
COPY ["set_tomcat.sh", "./"]
RUN chmod +x set_tomcat.sh
EXPOSE 8080
CMD ["set_tomcat.sh", "run"]