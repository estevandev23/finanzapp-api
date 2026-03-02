-- Habilitar extensión para generación de UUIDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabla de usuarios (estado inicial sin dos_factores_activado, se agrega en V2)
CREATE TABLE IF NOT EXISTS usuarios (
    id                   UUID         PRIMARY KEY,
    nombre               VARCHAR(255) NOT NULL,
    email                VARCHAR(255) NOT NULL UNIQUE,
    password             VARCHAR(255),
    telefono             VARCHAR(255) UNIQUE,
    oauth_provider       VARCHAR(255),
    oauth_provider_id    VARCHAR(255),
    telefono_verificado  BOOLEAN      NOT NULL DEFAULT false,
    activo               BOOLEAN      NOT NULL DEFAULT true,
    fecha_creacion       TIMESTAMP    NOT NULL,
    fecha_actualizacion  TIMESTAMP    NOT NULL
);

-- Tabla de metas financieras
CREATE TABLE IF NOT EXISTS metas_financieras (
    id                   UUID           PRIMARY KEY,
    usuario_id           UUID           NOT NULL REFERENCES usuarios(id),
    nombre               VARCHAR(255)   NOT NULL,
    descripcion          TEXT,
    monto_objetivo       NUMERIC(15, 2) NOT NULL,
    monto_actual         NUMERIC(15, 2) NOT NULL DEFAULT 0,
    fecha_limite         DATE,
    estado               VARCHAR(20)    NOT NULL,
    fecha_creacion       TIMESTAMP      NOT NULL,
    fecha_actualizacion  TIMESTAMP      NOT NULL
);

-- Tabla de categorías personalizadas
CREATE TABLE IF NOT EXISTS categorias_personalizadas (
    id                   UUID        PRIMARY KEY,
    usuario_id           UUID        NOT NULL REFERENCES usuarios(id),
    nombre               VARCHAR(100) NOT NULL,
    tipo                 VARCHAR(10)  NOT NULL,
    color                VARCHAR(7),
    icono                VARCHAR(50),
    activa               BOOLEAN      NOT NULL DEFAULT true,
    fecha_creacion       TIMESTAMP    NOT NULL,
    fecha_actualizacion  TIMESTAMP    NOT NULL,
    UNIQUE (usuario_id, nombre, tipo)
);

-- Tabla de ingresos
CREATE TABLE IF NOT EXISTS ingresos (
    id                          UUID           PRIMARY KEY,
    usuario_id                  UUID           NOT NULL REFERENCES usuarios(id),
    monto                       NUMERIC(15, 2) NOT NULL,
    categoria                   VARCHAR(50),
    descripcion                 TEXT,
    fecha                       DATE           NOT NULL,
    monto_ahorro                NUMERIC(15, 2),
    categoria_personalizada_id  UUID           REFERENCES categorias_personalizadas(id),
    fecha_creacion              TIMESTAMP      NOT NULL,
    fecha_actualizacion         TIMESTAMP      NOT NULL
);

-- Tabla de gastos
CREATE TABLE IF NOT EXISTS gastos (
    id                          UUID           PRIMARY KEY,
    usuario_id                  UUID           NOT NULL REFERENCES usuarios(id),
    monto                       NUMERIC(15, 2) NOT NULL,
    categoria                   VARCHAR(50),
    categoria_personalizada_id  UUID           REFERENCES categorias_personalizadas(id),
    descripcion                 TEXT,
    fecha                       DATE           NOT NULL,
    fecha_creacion              TIMESTAMP      NOT NULL,
    fecha_actualizacion         TIMESTAMP      NOT NULL
);

-- Tabla de ahorros
CREATE TABLE IF NOT EXISTS ahorros (
    id                   UUID           PRIMARY KEY,
    usuario_id           UUID           NOT NULL REFERENCES usuarios(id),
    ingreso_id           UUID           REFERENCES ingresos(id),
    meta_id              UUID           REFERENCES metas_financieras(id),
    monto                NUMERIC(15, 2) NOT NULL,
    descripcion          TEXT,
    fecha                DATE           NOT NULL,
    fecha_creacion       TIMESTAMP      NOT NULL,
    fecha_actualizacion  TIMESTAMP      NOT NULL
);

-- Tabla de dispositivos WhatsApp
CREATE TABLE IF NOT EXISTS dispositivos (
    id                        UUID         PRIMARY KEY,
    usuario_id                UUID         NOT NULL REFERENCES usuarios(id),
    numero_whatsapp           VARCHAR(255) NOT NULL,
    nombre_dispositivo        VARCHAR(255),
    token_dispositivo         VARCHAR(255) UNIQUE,
    activo                    BOOLEAN      NOT NULL DEFAULT false,
    verificado                BOOLEAN      NOT NULL DEFAULT false,
    codigo_verificacion       VARCHAR(255),
    fecha_expiracion_codigo   TIMESTAMP,
    ultima_conexion           TIMESTAMP,
    fecha_creacion            TIMESTAMP    NOT NULL,
    fecha_actualizacion       TIMESTAMP    NOT NULL
);

-- Tabla de códigos de verificación (OTP)
CREATE TABLE IF NOT EXISTS codigos_verificacion (
    id                UUID        PRIMARY KEY,
    usuario_id        UUID        NOT NULL REFERENCES usuarios(id),
    codigo            VARCHAR(6)  NOT NULL,
    tipo              VARCHAR(30) NOT NULL,
    usado             BOOLEAN     NOT NULL DEFAULT false,
    fecha_creacion    TIMESTAMP   NOT NULL,
    fecha_expiracion  TIMESTAMP   NOT NULL
);

-- Tabla de deudas (DEUDA: dinero que debes; PRESTAMO: dinero que prestaste)
CREATE TABLE IF NOT EXISTS deudas (
    id                   UUID           PRIMARY KEY,
    usuario_id           UUID           NOT NULL REFERENCES usuarios(id),
    tipo                 VARCHAR(20)    NOT NULL,
    descripcion          VARCHAR(500),
    entidad              VARCHAR(200),
    monto_total          NUMERIC(15, 2) NOT NULL,
    monto_abonado        NUMERIC(15, 2) NOT NULL DEFAULT 0,
    monto_restante       NUMERIC(15, 2) NOT NULL,
    estado               VARCHAR(20)    NOT NULL,
    fecha_inicio         DATE,
    fecha_limite         DATE,
    fecha_creacion       TIMESTAMP      NOT NULL,
    fecha_actualizacion  TIMESTAMP
);

-- Tabla de abonos a deudas/préstamos (historial de pagos)
CREATE TABLE IF NOT EXISTS abonos_deuda (
    id             UUID           PRIMARY KEY,
    deuda_id       UUID           NOT NULL REFERENCES deudas(id),
    monto          NUMERIC(15, 2) NOT NULL,
    descripcion    VARCHAR(500),
    fecha_abono    TIMESTAMP      NOT NULL,
    fecha_creacion TIMESTAMP      NOT NULL
);
