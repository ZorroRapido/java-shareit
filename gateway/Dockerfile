FROM amazoncorretto:11
COPY target/*.jar shareit-gateway.jar
#RUN bin/bash -c 'ln -snf /usr/share/zoneinfo/Europe/Moscow /etc/localtime'
ENTRYPOINT ["java", "-jar", "/shareit-gateway.jar"]