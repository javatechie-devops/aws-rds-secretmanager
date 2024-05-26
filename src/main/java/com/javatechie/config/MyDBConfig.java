package com.javatechie.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import javax.sql.DataSource;

@Configuration
public class MyDBConfig {

    @Bean
    //@Primary
    public DataSource dataSource() throws JsonProcessingException {
        String secret = getSecret();
        JsonNode secretNode = new ObjectMapper().readTree(secret);
        String userName = secretNode.get("username").asText();
        String pwd = secretNode.get("password").asText();
        String host = secretNode.get("host").asText();
        String port = secretNode.get("port").asText();
        String dbName = secretNode.get("dbname").asText();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + dbName);
        dataSource.setUsername(userName);
        dataSource.setPassword(pwd);
        return dataSource;
    }

    public static String getSecret() {
        String secretName = "mydb-secret";
        Region region = Region.of("ap-south-1");

        AwsBasicCredentials awsBasicCredentials=  AwsBasicCredentials.create("","");

        // Create a Secrets Manager client
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse getSecretValueResponse = client.getSecretValue(getSecretValueRequest);

        return getSecretValueResponse.secretString();
    }
}
