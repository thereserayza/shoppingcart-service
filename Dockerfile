FROM java:8
VOLUME /tmp
ADD shoppingcart-service*.jar shoppingcart-service.jar
RUN sh -c 'touch /shoppingcart-service.jar'
ENV JAVA_OPTS="-Xmx256m -Xms128m"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /shoppingcart-service.jar" ]