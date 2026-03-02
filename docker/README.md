# Docker - FinanzApp

## Estructura

```
docker/
├── docker-compose.yml      # Compose completo (PostgreSQL + App)
├── docker-compose.dev.yml  # Solo PostgreSQL para desarrollo local
├── init-db/
│   └── 01-init.sql         # Script de inicialización de BD
├── .env.example            # Plantilla de variables de entorno
└── README.md               # Este archivo
```

## Uso

### Desarrollo local (solo PostgreSQL)

```bash
# Levantar solo la base de datos
cd docker
docker-compose -f docker-compose.dev.yml up -d

# Ver logs
docker-compose -f docker-compose.dev.yml logs -f

# Detener
docker-compose -f docker-compose.dev.yml down
```

Luego ejecutar la aplicación localmente:
```bash
cd ..
./mvnw spring-boot:run
```

### Producción (PostgreSQL + App)

```bash
# Crear archivo .env con las variables
cp .env.example .env
# Editar .env con valores seguros

# Construir y levantar
docker-compose up -d --build

# Ver logs
docker-compose logs -f finanzapp

# Detener
docker-compose down
```

### Solo construir la imagen

```bash
cd ..
docker build -t finanzapp:latest .
```

## Conexión a PostgreSQL

- **Host**: localhost
- **Puerto**: 5432
- **Base de datos**: finanzapp
- **Usuario**: postgres (o el configurado en .env)
- **Password**: postgres (o el configurado en .env)

### String de conexión

```
jdbc:postgresql://localhost:5432/finanzapp
```

## Comandos útiles

```bash
# Acceder a la base de datos
docker exec -it finanzapp-db-dev psql -U postgres -d finanzapp

# Ver tablas
\dt

# Reiniciar con datos limpios
docker-compose -f docker-compose.dev.yml down -v
docker-compose -f docker-compose.dev.yml up -d
```
