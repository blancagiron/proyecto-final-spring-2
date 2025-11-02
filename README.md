# Proyecto Final - Spring Boot RESTful API

## Descripción

API RESTful completa desarrollada con Spring Boot que integra:

- CRUDs para usuarios, productos, pedidos y países
- Relaciones complejas entre entidades
- Seguridad con JWT (JSON Web Tokens)
- Validaciones con Jakarta Validation
- Control de acceso basado en roles (ADMIN/USER)
- Arquitectura hexagonal
- Dockerización completa

## Tecnologías Utilizadas

- **Java 17**
- **Spring Boot 3.2.8**
- **Spring Security** con JWT
- **Spring Data JPA**
- **PostgreSQL** como base de datos
- **MapStruct** para mapeo de DTOs
- **Lombok** para reducir código boilerplate
- **Docker & Docker Compose**
- **Maven** como gestor de dependencias

## Estructura del Proyecto

```
proyecto-final/
├── domain/           # Capa de dominio (modelos, excepciones, ports)
├── application/      # Capa de aplicación (casos de uso, servicios)
└── infrastructure/   # Capa de infraestructura
    ├── api/         # Controllers, DTOs, Security
    └── jpa/         # Repositories, Entities, Adapters
```

## Requisitos Previos

- Docker y Docker Compose instalados
- Java 17+ (solo si se ejecuta sin Docker)
- Maven 3.9+ (solo si se ejecuta sin Docker)

## Instalación y Ejecución

### Opción 1: Con Docker (Recomendado)

1. **Clonar el repositorio:**

```bash
git clone <repository-url>
cd proyecto-final
```

2. **Construir y levantar los contenedores:**

```bash
docker-compose up --build
```

3. **La aplicación estará disponible en:**

- API: http://localhost:8080
- pgAdmin: http://localhost:5050 (admin@admin.com / admin)
- PostgreSQL: localhost:5432

### Opción 2: Ejecución Local

1. **Iniciar PostgreSQL:**

```bash
docker-compose up db
```

2. **Compilar el proyecto:**

```bash
mvn clean install
```

3. **Ejecutar la aplicación:**

```bash
cd infrastructure/api
mvn spring-boot:run
```

## Credenciales por Defecto

### Usuario Administrador

- **Email:** admin@example.com
- **Password:** Admin123

### Usuario Normal

- **Email:** user@example.com
- **Password:** User123

## Endpoints Principales

### Autenticación

- `POST /auth/register` - Registro de nuevo usuario
- `POST /auth/login` - Login y obtención de token JWT

### Usuarios (requiere autenticación)

- `GET /users` - Listar todos los usuarios (solo ADMIN)
- `GET /users/{id}` - Obtener usuario por ID (solo ADMIN)
- `POST /users` - Crear usuario (solo ADMIN)
- `PUT /users/{id}` - Actualizar usuario (solo ADMIN)
- `DELETE /users/{id}` - Eliminar usuario (soft delete) (solo ADMIN)
- `PATCH /users/{id}/country` - Asignar país (ADMIN o el propio USER)

### Productos

- `GET /products` - Listar productos con filtros opcionales (ADMIN y USER)
- `GET /products/{id}` - Obtener producto por ID (ADMIN y USER)
- `POST /products` - Crear producto (solo ADMIN)
- `PUT /products/{id}` - Actualizar producto (solo ADMIN)
- `DELETE /products/{id}` - Eliminar producto (solo ADMIN)

### Pedidos

- `GET /orders` - Listar pedidos (ADMIN ve todos, USER solo los suyos)
- `GET /orders/{id}` - Obtener pedido por ID
- `POST /orders` - Crear pedido (ADMIN y USER)
- `PUT /orders/{id}` - Actualizar pedido (solo ADMIN)
- `DELETE /orders/{id}` - Eliminar pedido (solo ADMIN)

### Países

- `GET /countries` - Listar países (ADMIN y USER)
- `GET /countries/{code}` - Obtener país por código (ADMIN y USER)
- `POST /countries` - Crear país (solo ADMIN)
- `PUT /countries/{code}` - Actualizar país (solo ADMIN)
- `DELETE /countries/{code}` - Eliminar país (solo ADMIN)

## Ejemplo de Uso

### 1. Registro de Usuario

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "password": "Test123"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123"
  }'
```

Respuesta:

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "email": "test@example.com",
  "role": "ROLE_USER"
}
```

### 3. Usar Token para Acceder a Endpoints Protegidos

```bash
curl -X GET http://localhost:8080/products \
  -H "Authorization: Bearer <tu-token-jwt>"
```

### 4. Buscar Productos con Filtros

```bash
curl -X GET "http://localhost:8080/products?name=laptop&minPrice=1000&maxPrice=2000&status=AVAILABLE" \
  -H "Authorization: Bearer <tu-token-jwt>"
```

### 5. Crear un Pedido

```bash
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer <tu-token-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "orderProducts": [
      {
        "productId": 1,
        "amount": 2
      },
      {
        "productId": 3,
        "amount": 1
      }
    ]
  }'
```

## Validaciones Implementadas

### Usuarios

- Email único y formato válido
- Contraseña mínimo 8 caracteres con mayúsculas, minúsculas y números
- Nombre completo no vacío

### Productos

- Nombre no vacío
- Precio positivo
- Estado requerido (AVAILABLE/DISCONTINUED)

### Pedidos

- Usuario requerido
- Lista de productos no vacía
- Cantidades positivas

## Control de Acceso

| Endpoint | ADMIN | USER |
|----------|-------|------|
| /users | ✅ Completo | ❌ Sin acceso |
| /products | ✅ Completo | ✅ Solo GET con filtros |
| /orders | ✅ Ve y modifica todos | ✅ Solo ve/crea los suyos |
| /countries | ✅ Completo | ✅ Solo GET |
| /users/{id}/country | ✅ Modifica cualquier usuario | ✅ Solo modifica el suyo |

## Manejo de Errores

La API devuelve errores estructurados:

```json
{
  "timestamp": "2025-10-29",
  "httpCode": 404,
  "message": "User not found with id: 123"
}
```

Códigos HTTP comunes:

- `200 OK` - Operación exitosa
- `201 Created` - Recurso creado
- `204 No Content` - Eliminación exitosa
- `400 Bad Request` - Datos inválidos
- `401 Unauthorized` - No autenticado
- `403 Forbidden` - Sin permisos
- `404 Not Found` - Recurso no encontrado
- `409 Conflict` - Conflicto (ej: email duplicado)
- `422 Unprocessable Entity` - Validación fallida

## Logging

Los logs se configuran en diferentes niveles:

- `INFO`: Información general de la aplicación
- `DEBUG`: Información detallada para debugging (SQL, Security)
- `ERROR`: Errores de la aplicación

## Base de Datos

### Modelo de Datos

```
users
├── id (PK)
├── role
├── full_name
├── email (unique)
├── password (encrypted)
├── created_at
├── is_active
└── country_code (FK)

countries
├── code (PK)
└── name

products
├── id (PK)
├── name
├── price
├── product_status
└── creation_date

orders
├── id (PK)
├── user_id (FK)
├── status
└── creation_date

orders_products (many-to-many)
├── order_id (PK, FK)
├── product_id (PK, FK)
└── amount
```

## Parar la Aplicación

```bash
docker-compose down
```

Para eliminar también los volúmenes (datos):

```bash
docker-compose down -v
```

## Testing

Los tests se ejecutarán con:

```bash
mvn test
```

## SonarQube (Análisis de Código)

Para ejecutar análisis de código con SonarQube:

```bash
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=proyecto-final \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<your-token>
```

## Autor

Blanca - Proyecto Final Spring Boot


