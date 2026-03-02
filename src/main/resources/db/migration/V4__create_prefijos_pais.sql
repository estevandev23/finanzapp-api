-- Tabla de referencia para prefijos telefónicos por país
-- Permite agregar más países en el futuro sin cambios de código
CREATE TABLE IF NOT EXISTS prefijos_pais (
    id                UUID        PRIMARY KEY,
    nombre            VARCHAR(100) NOT NULL,
    codigo_iso        VARCHAR(3)   NOT NULL UNIQUE,
    prefijo_telefono  VARCHAR(10)  NOT NULL,
    bandera_emoji     VARCHAR(10),
    activo            BOOLEAN      NOT NULL DEFAULT true
);

INSERT INTO prefijos_pais (id, nombre, codigo_iso, prefijo_telefono, bandera_emoji, activo)
VALUES (gen_random_uuid(), 'Colombia', 'CO', '+57', '🇨🇴', true)
ON CONFLICT (codigo_iso) DO NOTHING;
