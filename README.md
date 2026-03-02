# FinanzApp — Backend (Spring Boot)

> **Nota:** Este proyecto forma parte del monorepo `proyecto-grado`.
> El `docker-compose.yml` unificado y la documentación completa del sistema se encuentran en `../README.md`.

API REST para gestión de finanzas personales, construida con **Java 21** y **Spring Boot 3** siguiendo una **arquitectura hexagonal** (puertos y adaptadores).

---

## Tabla de contenido

- [Stack tecnológico](#stack-tecnológico)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Servicios y lógica de negocio](#servicios-y-lógica-de-negocio)
- [Modelos de dominio](#modelos-de-dominio)
- [Seguridad (JWT)](#seguridad-jwt)
- [Endpoints de la API](#endpoints-de-la-api)
- [Configuración](#configuración)
- [Docker](#docker)
- [Ejecutar localmente (sin Docker)](#ejecutar-localmente-sin-docker)
- [Tests](#tests)

---

## Stack tecnológico

| Tecnología | Versión | Uso |
|-----------|---------|-----|
| Java | 21 | Lenguaje del backend |
| Spring Boot | 3.x | Framework principal |
| Spring Security | 6.x | Autenticación y autorización |
| Spring Data JPA | 3.x | Acceso a datos |
| Hibernate | 6.x | ORM (dialect PostgreSQL) |
| PostgreSQL | 16 | Base de datos relacional |
| JWT (jjwt) | — | Emisión y validación de tokens |
| Lombok | — | Reducción de boilerplate |
| SpringDoc (Swagger) | — | Documentación OpenAPI |
| Maven Wrapper | 3.9+ | Build tool |

---

## Estructura del proyecto

```
finanzapp/
├── Dockerfile                         ← Imagen Docker multi-stage (JDK 21 Alpine)
├── pom.xml                            ← Dependencias Maven
├── mvnw / mvnw.cmd                    ← Maven Wrapper
├── docker/
│   ├── docker-compose.yml             ← Compose sólo del backend + postgres
│   ├── docker-compose.dev.yml         ← Compose sólo de postgres (dev local)
│   └── init-db/
│       └── 01-init.sql                ← Habilita uuid-ossp en PostgreSQL
└── src/
    ├── main/
    │   ├── java/com/finanzapp/
    │   │   ├── FinanzappApplication.java          ← Punto de entrada
    │   │   ├── application/
    │   │   │   └── service/                       ← Casos de uso
    │   │   │       ├── AuthService.java
    │   │   │       ├── UsuarioService.java
    │   │   │       ├── IngresoService.java
    │   │   │       ├── GastoService.java
    │   │   │       ├── AhorroService.java
    │   │   │       ├── MetaFinancieraService.java
    │   │   │       ├── BalanceService.java
    │   │   │       └── DispositivoService.java
    │   │   ├── domain/
    │   │   │   ├── model/                         ← Entidades de dominio
    │   │   │   ├── port/                          ← Interfaces (in / out)
    │   │   │   └── exception/                     ← Excepciones de dominio
    │   │   └── infrastructure/
    │   │       ├── adapter/
    │   │       │   ├── in/                        ← Controladores REST
    │   │       │   └── out/persistence/           ← Repos JPA + mappers
    │   │       ├── config/                        ← Beans de configuración
    │   │       └── security/                      ← JwtService, filtros
    │   └── resources/
    │       └── application.yml                    ← Configuración de la app
    └── test/
        └── java/com/finanzapp/
            └── FinanzappApplicationTests.java
```

---

## Servicios y lógica de negocio

Cada servicio implementa un puerto de entrada (`*UseCase`) y encierra la lógica de negocio. Los detalles de persistencia se delegan a los puertos de salida (`*RepositoryPort`).

### `AuthService`
Gestiona autenticación y sesión:
- `login(email, password)` — valida credenciales con `AuthenticationManager`, devuelve JWT.
- `loginWhatsapp(numeroWhatsapp, codigoVerificacion)` — verifica el OTP del dispositivo, actualiza estado y devuelve JWT.
- `registrar(usuario)` — registra un nuevo usuario con contraseña encriptada.
- `refreshToken(token)` — emite un nuevo JWT a partir de un token válido.
- `logout(token)` — el cierre de sesión se gestiona en el cliente (el token no se invalida en servidor en esta versión).

### `UsuarioService`
CRUD completo de usuarios:
- `registrar` — asigna UUID, encripta contraseña, marca `activo = true`.
- `obtenerPorId` / `obtenerPorEmail` — búsqueda con excepción si no existe.
- `actualizar` — actualiza nombre y teléfono (campos opcionales).
- `eliminar` — baja lógica: `activo = false`.
- `cambiarPassword` — valida contraseña actual antes de actualizar.

### `IngresoService`
Gestión de ingresos del usuario:
- `registrar` — persiste ingreso; si no se indica fecha, usa la fecha actual; si no hay `montoAhorro`, lo inicializa en cero.
- `listarPorUsuario` / `listarPorPeriodo` / `listarPorCategoria`.
- `obtenerTotalIngresos` / `obtenerTotalIngresosPorPeriodo`.
- `actualizar` / `eliminar`.

### `GastoService`
Gestión de gastos del usuario:
- `registrar` — persiste gasto; fecha por defecto = hoy.
- `listarPorUsuario` / `listarPorPeriodo` / `listarPorCategoria`.
- `obtenerTotalGastos` / `obtenerTotalGastosPorPeriodo`.
- `obtenerDesglosePorCategoria` / `obtenerDesglosePorCategoriaPorPeriodo` — devuelve `Map<CategoriaGasto, BigDecimal>`.
- `actualizar` / `eliminar`.

### `AhorroService`
Gestión de ahorros, opcionalmente vinculados a una meta:
- `registrar` — fecha por defecto = hoy.
- `listarPorUsuario` / `listarPorPeriodo` / `listarPorMeta`.
- `obtenerTotalAhorros` / `obtenerTotalAhorrosPorPeriodo`.
- `actualizar` / `eliminar`.

### `MetaFinancieraService`
Metas de ahorro con seguimiento de progreso:
- `crear` — inicializa `montoActual = 0` y `estado = ACTIVA`.
- `obtenerPorId` — calcula `montoActual` sumando todos los ahorros asociados a la meta.
- `listarPorUsuario` / `listarPorEstado` — actualiza montos actuales en cada consulta.
- `registrarProgreso(metaId, monto)` — abona a la meta y la marca `COMPLETADA` si supera el objetivo.
- `cambiarEstado` / `actualizar` / `eliminar`.

### `BalanceService`
Calculadora de balance financiero (solo lectura):
- `obtenerBalanceGeneral(usuarioId)` — totalIngresos − totalGastos − totalAhorros.
- `obtenerBalancePorPeriodo(usuarioId, fechaInicio, fechaFin)` — igual pero acotado a un período.

### `DispositivoService`
Administración de dispositivos para login por WhatsApp:
- `registrar(usuarioId, numeroWhatsapp, nombreDispositivo)` — genera código OTP de 6 dígitos con expiración de 10 minutos.
- `verificar(numeroWhatsapp, codigoVerificacion)` — valida OTP y marca el dispositivo como `verificado = true`.
- `obtenerPorNumeroWhatsapp` — lanza `DispositivoNoVerificadoException` si no está verificado.
- `generarNuevoCodigo(dispositivoId)` — renueva el OTP.
- `desactivar` / `eliminar`.

---

## Modelos de dominio

Todos los modelos usan Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) y UUIDs como identificadores.

### `Usuario`
```
id · nombre · email · password · telefono · activo · fechaCreacion · fechaActualizacion
```

### `Ingreso`
```
id · usuarioId · monto · categoria(CategoriaIngreso) · descripcion
fecha · montoAhorro · fechaCreacion · fechaActualizacion
─ getMontoDisponible() → monto - montoAhorro
```

### `Gasto`
```
id · usuarioId · monto · categoria(CategoriaGasto) · descripcion · fecha
fechaCreacion · fechaActualizacion
```

### `Ahorro`
```
id · usuarioId · ingresoId · metaId · monto · descripcion · fecha
fechaCreacion · fechaActualizacion
```

### `MetaFinanciera`
```
id · usuarioId · nombre · descripcion · montoObjetivo · montoActual
fechaLimite · estado(EstadoMeta) · fechaCreacion · fechaActualizacion
─ getPorcentajeAvance()  → (montoActual / montoObjetivo) * 100
─ getMontoRestante()     → montoObjetivo - montoActual
─ isCompletada()         → montoActual >= montoObjetivo
```

### `Dispositivo`
```
id · usuarioId · numeroWhatsapp · nombreDispositivo · tokenDispositivo
activo · verificado · codigoVerificacion · fechaExpiracionCodigo
ultimaConexion · fechaCreacion · fechaActualizacion
```

### `Balance` (objeto calculado, no persistido)
```
usuarioId · totalIngresos · totalGastos · totalAhorros · dineroDisponible
─ calcular(ingresos, gastos, ahorros) → Balance  [método estático]
```

### Enumeraciones

| Enum | Valores |
|------|---------|
| `CategoriaIngreso` | `TRABAJO_PRINCIPAL`, `TRABAJO_EXTRA`, `GANANCIAS_ADICIONALES`, `INVERSIONES`, `OTROS` |
| `CategoriaGasto` | `COMIDA`, `PAREJA`, `COMPRAS`, `TRANSPORTE`, `SERVICIOS`, `ENTRETENIMIENTO`, `SALUD`, `EDUCACION`, `OTROS` |
| `EstadoMeta` | `ACTIVA`, `COMPLETADA`, `CANCELADA` |

---

## Seguridad (JWT)

- Clase `JwtService` en `infrastructure/security` — genera y valida tokens firmados con `JWT_SECRET`.
- Expiración de acceso: `JWT_EXPIRATION` ms (por defecto 24 h = 86 400 000 ms).
- Expiración de refresh: `JWT_REFRESH_EXPIRATION` ms (por defecto 7 días).
- Todas las rutas bajo `/api/v1/**` están protegidas salvo `/api/v1/auth/**`.
- Header requerido: `Authorization: Bearer <token>`

---

## Endpoints de la API

> Base URL: `http://localhost:8080/api/v1`
> Documentación interactiva: `http://localhost:8080/swagger-ui.html`

### Autenticación (`/auth`)

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| POST | `/auth/register` | ✗ | Registrar usuario |
| POST | `/auth/login` | ✗ | Login email/password → JWT |
| POST | `/auth/login/whatsapp` | ✗ | Login WhatsApp + OTP → JWT |
| POST | `/auth/refresh` | ✓ | Renovar token JWT |
| POST | `/auth/logout` | ✓ | Cerrar sesión |

**Ejemplo de login:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"juan@ejemplo.com","password":"secreta123"}'
# → { "success": true, "data": { "token": "eyJ...", "usuarioId": "...", "nombre": "Juan" } }
```

### Ingresos (`/ingresos`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/ingresos` | Registrar ingreso |
| GET | `/ingresos` | Listar todos |
| GET | `/ingresos/periodo?fechaInicio=YYYY-MM-DD&fechaFin=YYYY-MM-DD` | Por período |
| GET | `/ingresos/categoria/{categoria}` | Por categoría |
| GET | `/ingresos/total` | Total general |
| GET | `/ingresos/total/periodo?fechaInicio=&fechaFin=` | Total por período |
| PUT | `/ingresos/{id}` | Actualizar |
| DELETE | `/ingresos/{id}` | Eliminar |

### Gastos (`/gastos`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/gastos` | Registrar gasto |
| GET | `/gastos` | Listar todos |
| GET | `/gastos/periodo?fechaInicio=&fechaFin=` | Por período |
| GET | `/gastos/categoria/{categoria}` | Por categoría |
| GET | `/gastos/total` | Total general |
| GET | `/gastos/total/periodo?fechaInicio=&fechaFin=` | Total por período |
| GET | `/gastos/desglose` | Desglose por categoría |
| GET | `/gastos/desglose/periodo?fechaInicio=&fechaFin=` | Desglose por período |
| PUT | `/gastos/{id}` | Actualizar |
| DELETE | `/gastos/{id}` | Eliminar |

### Ahorros (`/ahorros`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/ahorros` | Registrar ahorro |
| GET | `/ahorros` | Listar todos |
| GET | `/ahorros/periodo?fechaInicio=&fechaFin=` | Por período |
| GET | `/ahorros/meta/{metaId}` | Por meta |
| GET | `/ahorros/total` | Total general |
| GET | `/ahorros/total/periodo?fechaInicio=&fechaFin=` | Total por período |
| PUT | `/ahorros/{id}` | Actualizar |
| DELETE | `/ahorros/{id}` | Eliminar |

### Metas financieras (`/metas`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/metas` | Crear meta |
| GET | `/metas` | Listar todas |
| GET | `/metas/{id}` | Detalle (con % avance) |
| GET | `/metas/estado/{estado}` | Filtrar por estado |
| PUT | `/metas/{id}` | Actualizar |
| POST | `/metas/{id}/progreso` | Abonar a la meta |
| PATCH | `/metas/{id}/estado` | Cambiar estado |
| DELETE | `/metas/{id}` | Eliminar |

### Balance (`/balance`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/balance` | Balance general |
| GET | `/balance/periodo?fechaInicio=&fechaFin=` | Balance por período |

### Dispositivos y WhatsApp (`/dispositivos`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/dispositivos` | Registrar dispositivo (genera OTP) |
| POST | `/dispositivos/verificar` | Verificar OTP |
| GET | `/dispositivos` | Listar dispositivos del usuario |
| POST | `/dispositivos/{id}/codigo` | Generar nuevo OTP |
| PATCH | `/dispositivos/{id}/desactivar` | Desactivar dispositivo |
| DELETE | `/dispositivos/{id}` | Eliminar dispositivo |

---

## Configuración

El archivo principal es `src/main/resources/application.yml`. Las propiedades sensibles se leen desde variables de entorno:

| Propiedad | Variable de entorno | Valor por defecto |
|-----------|--------------------|--------------------|
| Datasource URL | `DATABASE_URL` | `jdbc:postgresql://localhost:5432/pgfinanzapp` |
| Usuario BD | `DB_USERNAME` | `postgres` |
| Contraseña BD | `DB_PASSWORD` | `postgres` |
| JWT Secret | `JWT_SECRET` | *(valor por defecto largo)* |
| JWT Expiración | `JWT_EXPIRATION` | `86400000` ms |
| JWT Refresh | `JWT_REFRESH_EXPIRATION` | `604800000` ms |
| DDL Hibernate | `JPA_DDL_AUTO` | `update` |
| Show SQL | `JPA_SHOW_SQL` | `false` |
| Log level | `LOG_LEVEL` | `INFO` |

---

## Docker

El backend tiene su propio `Dockerfile` (multi-stage, Eclipse Temurin JDK 21 Alpine):

```
Stage 1 (builder): descarga dependencias Maven, compila y empaqueta el JAR.
Stage 2 (runner):  imagen JRE 21 mínima con usuario no-root. Expone el puerto 8080.
```

Health check integrado: `wget http://localhost:8080/actuator/health`

Para usar el **docker-compose unificado** (recomendado), ir a la carpeta padre:
```powershell
cd ..
docker compose up -d --build
```

Para levantar **sólo el backend** con su postgres:
```powershell
docker compose -f docker/docker-compose.yml up -d --build
```

Para levantar **sólo postgres** y correr el backend localmente:
```powershell
docker compose -f docker/docker-compose.dev.yml up -d
```

---

## Ejecutar localmente (sin Docker)

```powershell
# 1. Levantar PostgreSQL con Docker
docker compose -f docker/docker-compose.dev.yml up -d

# 2. Ejecutar la aplicación
./mvnw spring-boot:run

# La API queda disponible en:
#   http://localhost:8080/api/v1
#   http://localhost:8080/swagger-ui.html   ← Swagger UI
#   http://localhost:8080/actuator/health   ← Health check
```

---

## Tests

```powershell
# Ejecutar todos los tests
./mvnw test

# Compilar sin ejecutar tests
./mvnw clean package -DskipTests
```

---

## Licencia

MIT
