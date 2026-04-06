package com.consultingplatform.agent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class AgentWebConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    RestClient internalRestApiRestClient(@Value("${server.port:8080}") int serverPort) {
        return RestClient.builder().baseUrl("http://127.0.0.1:" + serverPort).build();
    }
}
