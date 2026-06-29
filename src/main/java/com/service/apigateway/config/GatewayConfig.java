package com.service.apigateway.config;

import com.service.apigateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    // URL сервисов читаем из переменных окружения
    // Локально → localhost:808x
    // В Docker → имя_контейнера:808x
    @Value("${services.auth-url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${services.product-url:http://localhost:8082}")
    private String productServiceUrl;

    @Value("${services.order-url:http://localhost:8083}")
    private String orderServiceUrl;

    @Value("${services.delivery-url:http://localhost:8084}")
    private String deliveryServiceUrl;

    @Value("${services.file-url:http://localhost:8085}")
    private String fileServiceUrl;

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
                .before(BeforeFilterFunctions.uri(authServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> productServiceRoute() {
        return RouterFunctions.route()
                .route(RequestPredicates.path("/api/products/**"),
                        HandlerFunctions.http())
                .before(this::addUserEmailHeader)
                .before(BeforeFilterFunctions.uri(productServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return RouterFunctions.route()
                .route(RequestPredicates.path("/api/orders/**"),
                        HandlerFunctions.http())
                .before(this::addUserEmailHeader)
                .before(BeforeFilterFunctions.uri(orderServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> deliveryServiceRoute() {
        return RouterFunctions.route()
                .route(RequestPredicates.path("/api/delivery/**"),
                        HandlerFunctions.http())
                .before(this::addUserEmailHeader)
                .before(BeforeFilterFunctions.uri(deliveryServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> fileServiceRoute() {
        return RouterFunctions.route()
                .route(RequestPredicates.path("/api/files/**"),
                        HandlerFunctions.http())
                .before(this::addUserEmailHeader)
                .before(BeforeFilterFunctions.uri(fileServiceUrl))
                .build();
    }
}