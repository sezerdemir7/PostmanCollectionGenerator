package com.demir.postmancollectiongenerator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.*;



@Configuration
public class PostmanCollectionGenerator {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void generatePostmanCollection() {
        String outputPath="src/main/resources/postman_collection.json";
        try {
            Map<String, Object> postmanCollection = new HashMap<>();
            postmanCollection.put("info", Map.of(
                    "_postman_id", UUID.randomUUID().toString(),
                    "name", appName,
                    "schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
            ));
            List<Map<String, Object>> itemList = new ArrayList<>();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : requestMappingHandlerMapping.getHandlerMethods().entrySet()) {
                RequestMappingInfo mappingInfo = entry.getKey();
                HandlerMethod handlerMethod = entry.getValue();

                Set<String> patterns = getPatterns(mappingInfo);
                if (patterns == null) {
                    continue;
                }

                Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();

                for (String pattern : patterns) {
                    for (RequestMethod method : methods) {
                        Map<String, Object> requestDetails = createRequestDetails(pattern, method, handlerMethod, objectMapper);

                        itemList.add(Map.of(
                                "name", handlerMethod.getMethod().getName(),
                                "request", requestDetails
                        ));
                    }
                }
            }

            postmanCollection.put("item", itemList);

            File file = new File(outputPath);
            objectMapper.writeValue(file, postmanCollection);
            System.out.println("Postman collection created successfully: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error while creating file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Set<String> getPatterns(RequestMappingInfo mappingInfo) {
        Set<String> patterns = null;
        if (mappingInfo.getPatternsCondition() != null) {
            patterns = mappingInfo.getPatternsCondition().getPatterns();
        } else if (mappingInfo.getPathPatternsCondition() != null) {
            patterns = mappingInfo.getPathPatternsCondition().getPatternValues();
        } else {
            System.out.println("No patterns found for method.");
        }
        return patterns;
    }

    private Map<String, Object> createRequestDetails(String pattern, RequestMethod method, HandlerMethod handlerMethod, ObjectMapper objectMapper) throws IOException {
        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("method", method.name());
        requestDetails.put("header", Collections.emptyList());

        // Dinamik port numarasını al
        String port = environment.getProperty("local.server.port", "8080"); // Default olarak 8080

        // URL configuration
        List<Map<String, String>> queryParameters = new ArrayList<>();
        StringBuilder rawUrl = new StringBuilder("http://localhost:" + port + pattern);

        // Query parameters and path variables preparation
        Map<String, Object> bodyContent = new HashMap<>();
        boolean hasRequestBody = false;

        for (Parameter parameter : handlerMethod.getMethod().getParameters()) {
            if (parameter.isAnnotationPresent(RequestBody.class)) {
                hasRequestBody = true;
                bodyContent.putAll(generateExampleJson(parameter.getType()));
            } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                String paramName = requestParam.value().isEmpty() ? parameter.getName() : requestParam.value();
                queryParameters.add(Map.of("key", paramName, "value", "sampleValue"));
            } else if (parameter.isAnnotationPresent(PathVariable.class)) {
                String paramName = parameter.getName();
                pattern = pattern.replace("{" + paramName + "}", "{" + paramName + "}");
            }
        }

        if (!queryParameters.isEmpty()) {
            rawUrl.append("?");
            rawUrl.append(String.join("&", queryParameters.stream()
                    .map(param -> param.get("key") + "=" + param.get("value"))
                    .toArray(String[]::new)));
        }

        requestDetails.put("url", Map.of(
                "raw", rawUrl.toString(),
                "protocol", "http",
                "host", List.of("localhost"),
                "port", port,
                "path", Arrays.asList(pattern.split("/")),
                "query", queryParameters
        ));

        if (hasRequestBody) {
            requestDetails.put("body", Map.of(
                    "mode", "raw",
                    "raw", objectMapper.writeValueAsString(bodyContent),
                    "options", Map.of("raw", Map.of("language", "json"))
            ));
        } else {
            requestDetails.put("body", Map.of("mode", "none"));
        }

        return requestDetails;
    }

    private Map<String, Object> generateExampleJson(Class<?> clazz) {
        Map<String, Object> exampleJson = new HashMap<>();

        for (var field : clazz.getDeclaredFields()) {
            if (field.getType().equals(String.class)) {
                exampleJson.put(field.getName(), "example");
            } else if (field.getType().equals(Long.class) || field.getType().equals(Long.TYPE)) {
                exampleJson.put(field.getName(), 0L);
            } else if (field.getType().equals(Integer.class) || field.getType().equals(Integer.TYPE)) {
                exampleJson.put(field.getName(), 0);
            } else if (field.getType().equals(Boolean.class) || field.getType().equals(Boolean.TYPE)) {
                exampleJson.put(field.getName(), false);
            } else if (field.getType().equals(Double.class) || field.getType().equals(Double.TYPE)) {
                exampleJson.put(field.getName(), 0.0);
            } else {
                exampleJson.put(field.getName(), "nestedObject");
            }
        }
        return exampleJson;
    }
}
