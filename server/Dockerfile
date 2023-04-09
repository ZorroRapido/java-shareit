FROM amazoncorretto:11
COPY target/*.jar shareit-server.jar
#RUN bin/bash -c 'touch /server.log'
RUN bin/bash -c 'ln -snf /usr/share/zoneinfo/Europe/Moscow /etc/localtime'
ENTRYPOINT ["java", "-jar", "/shareit-server.jar"]