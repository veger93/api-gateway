package com.service.apigateway.config;

import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return RouterFunctions.route()
                .route(RequestPredicates.path("/api/auth/**"),
                        HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri("http://localhost:8081"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> productServiceRoute() {
        return RouterFunctions.route()
                .route(RequestPredicates.path("/api/products/**"),
                        HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri("http://localhost:8082"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return RouterFunctions.route()
                .route(RequestPredicates.path("/api/orders/**"),
                        HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri("http://localhost:8083"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> deliveryServiceRoute() {
        return RouterFunctions.route()
                .route(RequestPredicates.path("/api/delivery/**"),
                        HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri("http://localhost:8084"))
                .build();
    }
}