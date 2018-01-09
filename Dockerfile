FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD target/shoppingcart-service*.jar shoppingcart-service.jar
RUN sh -c 'touch /shoppingcart-service.jar'
ENTRYPOINT [ "java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "shoppingcart-service.jar" ]