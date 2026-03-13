package com.ibe.ibe_blitz_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.graphql.server.webmvc.GraphQlHttpHandler;

@Configuration
public class GraphQlWebMvcConfig {

    @Bean
    public GraphQlHttpHandler graphQlHttpHandler(WebGraphQlHandler webGraphQlHandler) {
        return new CustomGraphQlHttpHandler(webGraphQlHandler);
    }
}
