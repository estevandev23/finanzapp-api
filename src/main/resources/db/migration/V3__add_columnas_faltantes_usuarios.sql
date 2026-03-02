-- Agrega columnas que fueron añadidas a UsuarioEntity después de la creación inicial
-- y que Hibernate ddl-auto: update no pudo agregar (columnas NOT NULL sin DEFAULT en tablas con datos)
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS telefono_verificado BOOLEAN NOT NULL DEFAULT false;

-- Columnas para autenticación OAuth (nullable, pero se agregan con IF NOT EXISTS por seguridad)
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS oauth_provider VARCHAR(255);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS oauth_provider_id VARCHAR(255);
