package com.service.apigateway.config;

import com.service.apigateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtUtil jwtUtil;

    // Добавляем заголовок X-User-Email если в запросе есть валидный токен
    private ServerRequest addUserEmailHeader(ServerRequest request) {
        String authHeader = request.headers().firstHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isTokenValid(token)) {
                String email = jwtUtil.extractEmail(token);
                return ServerRequest.from(request)
                        .header("X-User-Email", email)
                        .build();
            }
        }
        return request;
    }

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
                .before(this::addUserEmailHeader)
                .before(BeforeFilterFunctions.uri("http://localhost:8082"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return RouterFunctions.route()
                .route(RequestPredicates.path("/api/orders/**"),
                        HandlerFunctions.http())
                .before(this::addUserEmailHeader)
                .before(BeforeFilterFunctions.uri("http://localhost:8083"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> deliveryServiceRoute() {
        return RouterFunctions.route()
                .route(RequestPredicates.path("/api/delivery/**"),
                        HandlerFunctions.http())
                .before(this::addUserEmailHeader)
                .before(BeforeFilterFunctions.uri("http://localhost:8084"))
                .build();
    }
}