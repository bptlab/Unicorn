FROM ubuntu

WORKDIR /home

# Execute scriptbase image creation
COPY scripts/image-designer.sh .
RUN chmod +x image-designer.sh
RUN ./image-designer.sh
RUN rm image-designer.sh

# Tomcat-Port
EXPOSE 8080
# MySQL-Port
EXPOSE 3306

COPY scripts/container-designer.sh .
RUN chmod +x container-designer.sh 
ENTRYPOINT [ "./container-designer.sh" ]