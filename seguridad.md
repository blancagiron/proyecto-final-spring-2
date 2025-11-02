# Seguridad en tu Proyecto Spring Boot

## Resumen del Problema

El error **403 Forbidden** en el endpoint `PUT /users/{id}` se debía a que el código estaba **desactivando
accidentalmente** al usuario admin que hacía la petición, causando que perdiera acceso inmediatamente.

### Causa Raíz

```java
// ❌ PROBLEMA en UserUpdateDto.java
private boolean isActive;  // Primitivo: siempre tiene valor (false por defecto)

// ❌ PROBLEMA en UserServiceImpl.java  
existingUser.setActive(userWithNewData.isActive());  // Siempre asigna false
```

Cuando usas `boolean` (primitivo), **nunca puede ser null**, por lo que:

1. Se crea un DTO vacío → `isActive = false` automáticamente
2. Se copia al User → `isActive = false`
3. Se actualiza el usuario → **lo desactiva sin querer**
4. Si actualizas tu propio usuario admin → pierdes acceso → 403 Forbidden

### Solución

```java
// ✅ CORRECTO en UserUpdateDto.java
private Boolean isActive;  // Wrapper: puede ser null

// ✅ CORRECTO en UserController.java
if (dto.getIsActive() != null) {
    existingUser.setActive(dto.getIsActive());
}
```

---

## Arquitectura de Seguridad

Tu proyecto usa **JWT (JSON Web Tokens)** con **Spring Security**. Aquí está el flujo completo:

### 1. Componentes Principales

```
┌─────────────────────────────────────────────────────────────┐
│                    ARQUITECTURA DE SEGURIDAD                 │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐      ┌──────────────────┐                │
│  │   Cliente    │─────▶│  AuthController  │                │
│  │  (Postman)   │      │  /auth/login     │                │
│  └──────────────┘      └────────┬─────────┘                │
│         ▲                        │                           │
│         │                        ▼                           │
│         │              ┌──────────────────┐                 │
│         │              │ Authentication   │                 │
│         │              │    Manager       │                 │
│         │              └────────┬─────────┘                 │
│         │                       │                           │
│         │                       ▼                           │
│         │              ┌──────────────────┐                 │
│         │              │ UserDetailsService│                │
│         │              │ (carga usuario)  │                 │
│         │              └────────┬─────────┘                 │
│         │                       │                           │
│         │                       ▼                           │
│         │              ┌──────────────────┐                 │
│      Token             │ JwtTokenProvider │                 │
│         │◀─────────────│ (genera JWT)     │                 │
│         │              └──────────────────┘                 │
│         │                                                    │
│         │  Siguientes peticiones:                           │
│         │                                                    │
│         │              ┌──────────────────┐                 │
│         └─────────────▶│ JwtAuthFilter    │                 │
│                        │ (valida token)   │                 │
│                        └────────┬─────────┘                 │
│                                 │                            │
│                                 ▼                            │
│                        ┌──────────────────┐                 │
│                        │  Controller      │                 │
│                        │  @PreAuthorize   │                 │
│                        └──────────────────┘                 │
└─────────────────────────────────────────────────────────────┘
```

### 2. Flujo de Autenticación (Login)

**Paso a Paso:**

1. **Usuario envía credenciales**
   ```json
   POST /auth/login
   {
     "email": "admin@example.com",
     "password": "Admin123"
   }
   ```

2. **AuthController recibe la petición**
   ```java
   @PostMapping("/login")
   public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
       // Intenta autenticar
       Authentication authentication = authenticationManager.authenticate(
           new UsernamePasswordAuthenticationToken(
               loginRequest.getEmail(),
               loginRequest.getPassword()
           )
       );
   ```

3. **AuthenticationManager delega a UserDetailsService**
   ```java
   @Service
   public class CustomUserDetailsService implements UserDetailsService {
       @Override
       public UserDetails loadUserByUsername(String email) {
           // Busca el usuario en la BD
           User user = userRepository.findByEmail(email)
               .orElseThrow(() -> new UsernameNotFoundException("User not found"));
           
           // Verifica que esté activo
           if (!user.isActive()) {
               throw new UsernameNotFoundException("User is not active");
           }
           
           // Devuelve UserDetails con el rol
           return new org.springframework.security.core.userdetails.User(
               user.getEmail(),
               user.getPassword(),  // BCrypt hash
               getAuthorities(user) // [ROLE_ADMIN] o [ROLE_USER]
           );
       }
   }
   ```

4. **Spring Security verifica la contraseña**
    - Compara el hash BCrypt de la BD con la contraseña proporcionada
    - Si coincide → autenticación exitosa
    - Si no coincide → lanza BadCredentialsException

5. **Se genera el JWT**
   ```java
   String jwt = tokenProvider.generateToken(authentication);
   ```

   El token contiene:
   ```json
   {
     "sub": "admin@example.com",
     "roles": "ROLE_ADMIN",
     "iat": 1234567890,
     "exp": 1234654290
   }
   ```

6. **Se devuelve el token al cliente**
   ```json
   {
     "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxl...",
     "type": "Bearer",
     "email": "admin@example.com",
     "role": "ROLE_ADMIN"
   }
   ```

### 3. Flujo de Autorización (Peticiones siguientes)

**Cuando haces una petición a cualquier endpoint protegido:**

1. **Cliente envía token en header**
   ```
   GET /users/1
   Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
   ```

2. **JwtAuthenticationFilter intercepta la petición**
   ```java
   @Component
   public class JwtAuthenticationFilter extends OncePerRequestFilter {
       @Override
       protected void doFilterInternal(HttpServletRequest request, ...) {
           // 1. Extrae el token del header
           String jwt = getJwtFromRequest(request);
           
           // 2. Valida el token
           if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
               // 3. Extrae el email del token
               String username = tokenProvider.getUsernameFromToken(jwt);
               
               // 4. Carga los detalles del usuario
               UserDetails userDetails = userDetailsService.loadUserByUsername(username);
               
               // 5. Crea el objeto de autenticación
               UsernamePasswordAuthenticationToken authentication =
                   new UsernamePasswordAuthenticationToken(
                       userDetails, 
                       null, 
                       userDetails.getAuthorities()
                   );
               
               // 6. Establece la autenticación en el contexto de seguridad
               SecurityContextHolder.getContext().setAuthentication(authentication);
           }
           
           filterChain.doFilter(request, response);
       }
   }
   ```

3. **El Controller verifica los permisos**
   ```java
   @PutMapping("/{id}")
   @PreAuthorize("hasRole('ADMIN')")  // ← Verifica que el rol sea ADMIN
   public ResponseEntity<UserOutputDto> updateUser(...) {
       // Si llegas aquí, tienes permiso
   }
   ```

4. **Spring Security evalúa @PreAuthorize**
    - Lee los roles del Authentication object
    - Si tiene `ROLE_ADMIN` → permite acceso
    - Si NO tiene `ROLE_ADMIN` → lanza AccessDeniedException → 403 Forbidden

### 4. Configuración de Seguridad

**SecurityConfig.java** define las reglas:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Habilita @PreAuthorize
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)  // Desactiva CSRF (API REST)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Sin sesiones
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()  // Login sin auth
                .anyRequest().authenticated()             // Todo lo demás requiere auth
            )
            .addFilterBefore(
                jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class  // Filtro JWT antes que el de usuario/contraseña
            );
        
        return http.build();
    }
}
```

---

## Roles y Permisos en tu Sistema

### Roles Disponibles

```java
public enum Role {
    ROLE_ADMIN,
    ROLE_USER
}
```

### Matriz de Permisos

| Endpoint | ADMIN | USER |
|----------|-------|------|
| **Usuarios** |
| POST /users | ✅ Crea usuarios | ❌ Sin acceso |
| GET /users | ✅ Ve todos | ❌ Sin acceso |
| GET /users/{id} | ✅ Ve cualquiera | ❌ Sin acceso |
| PUT /users/{id} | ✅ Actualiza cualquiera | ❌ Sin acceso |
| DELETE /users/{id} | ✅ Desactiva cualquiera | ❌ Sin acceso |
| PATCH /users/{id}/country | ✅ Modifica cualquiera | ✅ Solo el suyo |
| **Productos** |
| POST /products | ✅ Crea | ❌ Sin acceso |
| GET /products | ✅ Ve todos | ✅ Ve todos |
| GET /products/{id} | ✅ Ve cualquiera | ✅ Ve cualquiera |
| PUT /products/{id} | ✅ Actualiza | ❌ Sin acceso |
| DELETE /products/{id} | ✅ Elimina | ❌ Sin acceso |
| **Pedidos** |
| POST /orders | ✅ Crea para cualquiera | ✅ Solo para sí mismo |
| GET /orders | ✅ Ve todos | ✅ Solo los suyos |
| GET /orders/{id} | ✅ Ve cualquiera | ✅ Solo los suyos |
| PUT /orders/{id} | ✅ Actualiza | ❌ Sin acceso |
| DELETE /orders/{id} | ✅ Elimina | ❌ Sin acceso |
| **Países** |
| POST /countries | ✅ Crea | ❌ Sin acceso |
| GET /countries | ✅ Ve todos | ✅ Ve todos |
| GET /countries/{code} | ✅ Ve cualquiera | ✅ Ve cualquiera |
| PUT /countries/{code} | ✅ Actualiza | ❌ Sin acceso |
| DELETE /countries/{code} | ✅ Elimina | ❌ Sin acceso |

---

## Ejemplo Completo de Uso

### 1. Login como Admin

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "Admin123"
  }'
```

**Respuesta:**

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxl...",
  "type": "Bearer",
  "email": "admin@example.com",
  "role": "ROLE_ADMIN"
}
```

### 2. Actualizar Usuario (con token)

```bash
curl -X PUT http://localhost:8080/users/2 \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Jane Doe Updated",
    "email": "jane.updated@example.com"
  }'
```

**Nota:** `isActive` no se envía, por lo que NO se modifica.

### 3. Actualizar Usuario incluyendo isActive

```bash
curl -X PUT http://localhost:8080/users/2 \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Jane Doe",
    "isActive": false
  }'
```

**Esto desactivará al usuario 2.**

---

## Debugging: Cómo Identificar Problemas de Seguridad

### 1. Verifica que el token sea válido

```bash
# El token tiene 3 partes separadas por puntos:
# header.payload.signature

# Decodifica el payload (parte 2):
echo "eyJzdWIiOiJhZG1pbkBleGFtcGxl..." | base64 -d
```

### 2. Revisa los logs

Tu proyecto tiene logging activado:

```properties
logging.level.org.springframework.security=DEBUG
```

Busca en los logs:

```
DEBUG o.s.s.a.d.DaoAuthenticationProvider : Authenticated user
DEBUG o.s.s.w.a.i.FilterSecurityInterceptor : Authorized filter invocation
```

### 3. Errores comunes

| Error | Causa | Solución |
|-------|-------|----------|
| 401 Unauthorized | Token inválido o expirado | Haz login de nuevo |
| 403 Forbidden | Usuario no tiene el rol necesario | Verifica que `isActive = true` y el rol sea correcto |
| 403 Forbidden después de update | Usuario se desactivó a sí mismo | ✅ Usa `Boolean` en lugar de `boolean` |

---

## Resumen de la Corrección

### Cambios Necesarios

1. **UserUpdateDto.java**
   ```java
   // ❌ ANTES
   private boolean isActive;
   
   // ✅ DESPUÉS
   private Boolean isActive;
   ```

2. **UserController.java**
   ```java
   // ❌ ANTES
   User userWithNewData = new User();
   userApiMapper.updateDomainFromInputDto(dto, userWithNewData);
   
   // ✅ DESPUÉS
   User existingUser = userService.findById(id).orElseThrow();
   if (dto.getIsActive() != null) {
       existingUser.setActive(dto.getIsActive());
   }
   ```

3. **UserServiceImpl.java**
   ```java
   // ❌ ANTES
   existingUser.setActive(userWithNewData.isActive());
   
   // ✅ DESPUÉS
   // Solo actualizar si no es null (ya manejado en el controller)
   ```

### Por Qué Funciona Ahora

1. `Boolean` puede ser `null` → no enviar el campo = no modificar
2. Solo se actualiza `isActive` si se envía explícitamente
3. El admin no se desactiva accidentalmente
4. Mantiene acceso → 200 OK en lugar de 403 Forbidden

---

## Mejoras Recomendadas

### 1. Prevenir que el admin se desactive a sí mismo

```java
@Override
public void update(Long userId, User userWithNewData) {
    User existingUser = userRepository.findById(userId).orElseThrow();
    
    // Obtener el usuario actual autenticado
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String currentEmail = auth.getName();
    
    // Si es el mismo usuario y tiene ROLE_ADMIN, no permitir desactivar
    if (existingUser.getEmail().equals(currentEmail) 
            && existingUser.getRole() == Role.ROLE_ADMIN
            && userWithNewData.isActive() != null 
            && !userWithNewData.isActive()) {
        throw new ForbiddenOperationException("Cannot deactivate your own admin account");
    }
    
    // ... resto del código
}
```

### 2. Logging mejorado

```java
log.info("User {} is updating user {} - isActive change: {} -> {}", 
    currentUserEmail, 
    userId, 
    existingUser.isActive(), 
    dto.getIsActive()
);
```

### 3. Auditoría de cambios

Considera agregar una tabla de auditoría para registrar quién modificó qué y cuándo.

---

## Conclusión

Tu sistema de seguridad es sólido y está bien implementado. El problema no era de seguridad en sí, sino de lógica de
negocio: estabas desactivando usuarios sin querer por usar tipos primitivos en lugar de wrappers.

**Lección clave:** En DTOs de actualización, **siempre usa wrappers** (`Boolean`, `Integer`, `Long`) en lugar de
primitivos (`boolean`, `int`, `long`) para poder distinguir entre "no enviado" (`null`) y "enviado con valor" (`true`/
`false`).