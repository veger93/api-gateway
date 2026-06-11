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
            "/api/auth/login"
    };

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        log.debug(">>> Входящий запрос: {} {}", method, path);

        // Публичные пути пропускаем без проверки
        if (isPublicPath(path)) {
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
        request.setAttribute("X-User-Email", email);

        return true;
    }

    private boolean isPublicPath(String path) {
        for (String publicPath : PUBLIC_PATHS) {
            if (path.equals(publicPath)) return true;
        }
        return false;
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
