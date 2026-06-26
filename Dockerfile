# Этап 1 — сборка
# Используем образ с JDK для компиляции
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Копируем файлы сборки
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

# Копируем исходный код
COPY src/ src/

# Собираем jar (пропускаем тесты чтобы ускорить сборку)
RUN ./gradlew bootJar -x test

# Этап 2 — запуск
# Используем образ только с JRE — он легче чем JDK
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Копируем собранный jar из первого этапа
COPY --from=builder /app/build/libs/*.jar app.jar

# Открываем порт
EXPOSE 8080

# Запускаем
ENTRYPOINT ["java", "-jar", "app.jar"]