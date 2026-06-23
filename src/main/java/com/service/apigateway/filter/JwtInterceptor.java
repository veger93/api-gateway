package com.service.apigateway.filter;

import com.service.apigateway.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    private static final String[] PUBLIC_PATHS = {
            "/api/auth/register",
            "/api/auth/login",
            "/api/products",
            "/api/products/categories"
    };

    // Публичные методы для приватных путей — например GET /api/products
    // открыт всем, но POST /api/products (создание товара) должен требовать токен
    private boolean isPublicRequest(String path, String method) {
        // auth пути — регистрация и логин, всегда публичны
        if (path.startsWith("/api/auth/")) {
            return true;
        }
        // Товары открыты на чтение для всех
        if (path.startsWith("/api/products") && method.equals("GET")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        log.debug(">>> Входящий запрос: {} {}", method, path);
        log.debug(">>> isPublicRequest результат: {}", isPublicRequest(path, method));

        // OPTIONS — preflight CORS запрос от браузера, всегда пропускаем
        if (method.equals("OPTIONS")) {
            log.debug(">>> OPTIONS preflight — пропускаем");
            return true;
        }

        // Публичные пути пропускаем без проверки
        if (isPublicRequest(path, method)) {
            log.debug(">>> Публичный путь — пропускаем");
            return true; // true = продолжаем обработку запроса
        }

        String authHeader = request.getHeader("Authorization");
        log.debug(">>> Authorization header: {}",
                authHeader != null ? "присутствует" : "отсутствует");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug(">>> Токен отсутствует — возвращаем 401");
            sendUnauthorized(response, "Токен отсутствует или имеет неверный формат");
            return false; // false = останавливаем обработку запроса
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            log.debug(">>> Токен невалидный — возвращаем 401");
            sendUnauthorized(response, "Токен недействителен или истёк");
            return false;
        }

        String email = jwtUtil.extractEmail(token);
        log.debug(">>> Токен валидный, пользователь: {}", email);

        // Добавляем email в атрибуты запроса — downstream сервисы смогут его прочитать
        request.setAttribute("authenticatedEmail", email);

        return true;
    }

    private void sendUnauthorized(HttpServletResponse response,
                                  String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(
                String.format(
                        "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\"}",
                        message
                )
        );
    }
}
