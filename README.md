# 🛒 FreshDrop — онлайн магазин продуктов

Fullstack приложение для заказа продуктов с доставкой.

## Стек технологий

**Backend:**
- Java 17, Spring Boot 4.x
- Spring Security + JWT авторизация
- Spring Cloud Gateway (API Gateway)
- PostgreSQL + Spring Data JPA / Hibernate
- Gradle (Groovy DSL)

**Frontend:**
- React 19 + Vite
- Tailwind CSS
- Axios

## Архитектура

Приложение построено на микросервисной архитектуре:

| Сервис | Порт | Назначение |
|---|---|---|
| api-gateway | 8080 | Роутинг, JWT фильтрация |
| auth-service | 8081 | Регистрация, логин, JWT токены |
| product-service | 8082 | Каталог товаров и категорий |
| order-service | 8083 | Корзина и заказы |
| delivery-service | 8084 | Адреса и статус доставки |
| file-service | 8085 | Загрузка картинок (Cloudinary) |
| frontend | 5173 | React SPA |

## Функциональность

- Регистрация и авторизация пользователей (JWT)
- Каталог товаров с фильтрацией по категориям
- Поиск товаров
- Корзина с изменением количества
- Оформление и отмена заказов
- История заказов
- Загрузка изображений через Cloudinary

## Запуск локально

### Требования
- Java 17+
- Node.js 20+
- PostgreSQL 15+

### База данных
```bash
docker run --name freshdrop-db \
  -e POSTGRES_PASSWORD=secret \
  -e POSTGRES_DB=foodshop_db \
  -p 5432:5432 -d postgres:15
```

### Backend — запускай каждый сервис в IntelliJ IDEA
Порядок запуска: auth-service → product-service → order-service → delivery-service → file-service → api-gateway

### Frontend
```bash
cd frontend
npm install
npm run dev
```

Открой http://localhost:5173