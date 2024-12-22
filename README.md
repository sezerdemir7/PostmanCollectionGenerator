## Postman Collection Generator User Guide

This library automatically scans all API endpoints in your Spring Boot projects and exports them in Postman collection format. This document provides guidance on adding the necessary dependencies and how to configure them.

### Requirements
- Java 11 or later.
- Spring Boot.

### Adding the Dependency

Add the following dependency to your project's `pom.xml` file:

```xml
<dependency>
    <groupId>com.github.sezerdemir7</groupId>
    <artifactId>PostmanCollectionGenerator</artifactId>
    <version>1.0.18</version>
</dependency>
```
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

### Adding the Service Class
You need to create a service class to create the Postman collection. The following code shows an example configuration required for this operation:

```java

import com.demir.postmancollectiongenerator.config.PostmanCollectionGenerator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Service
public class CollectionService {

 private final RequestMappingHandlerMapping requestMappingHandlerMapping;
 private final Environment environment;

 @Autowired
 public CollectionService(RequestMappingHandlerMapping requestMappingHandlerMapping, Environment environment) {
 this.requestMappingHandlerMapping = requestMappingHandlerMapping;
 this.environment = environment;

 }

@PostConstruct
public void init() {
PostmanCollectionGenerator postmanCollectionGenerator = new PostmanCollectionGenerator(requestMappingHandlerMapping, environment);
postmanCollectionGenerator.generatePostmanCollection();
}
}
```

### How to Start?
1. **Add Dependency**: Add the above dependency to your `pom.xml` file and reload Maven.
2. **Add Service Class**: Add the above `CollectionService` class to your project.
3. **Start Application**: When you start your Spring Boot application, Postman collection will be created automatically.
4. **Check Output**: The created collection will be saved in `src/main/resources/postman_collection.json` file.

### Important Notes
- The generated collection contains the HTTP methods of the API endpoints, URL structures, and optionally sample request bodies.
- The application's `local.server.port` value is automatically retrieved at runtime.

You can easily use the Postman Collection Generator in your project by following these instructions.






## License
This project is open-source and licensed under the MIT License.
