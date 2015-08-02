# HTTP Response Service

Java Dropwizard based HTTP Response service
--------

The goal of this project is to provide a simple service for testing HTTP response codes, response headers, slow responses and cookie responses. 

It was created to aid with cache configuration, so that caches could be configured to respect or override service responses. 


**Build**

mvn clean package

**Run**

java -jar target/dropwizard-response-service.jar server dropwizard-response-service.yml
