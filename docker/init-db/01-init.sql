-- Script de inicialización de la base de datos
-- Este script se ejecuta automáticamente cuando se crea el contenedor por primera vez

-- Crear extensión para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Crear esquema si no existe
CREATE SCHEMA IF NOT EXISTS public;

-- Comentario informativo
COMMENT ON DATABASE finanzapp IS 'Base de datos para la aplicación de finanzas personales FinanzApp';

-- Las tablas serán creadas automáticamente por Hibernate (ddl-auto: update)
-- Este script solo prepara la base de datos con extensiones necesarias
