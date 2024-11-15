package com.demir.postmancollectiongenerator;

import com.demir.postmancollectiongenerator.config.PostmanCollectionGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PostmanCollectionGeneratorApplication implements CommandLineRunner {


    private final PostmanCollectionGenerator postmanCollectionGenerator;

    public PostmanCollectionGeneratorApplication(PostmanCollectionGenerator postmanCollectionGenerator) {
        this.postmanCollectionGenerator = postmanCollectionGenerator;
    }

    public static void main(String[] args) {
        SpringApplication.run(PostmanCollectionGeneratorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        postmanCollectionGenerator.generatePostmanCollection("src/main/resources/postman_collection.json");

    }
}
