-- =========================================================
-- V16 - Presupuesto con bolsillos, snapshot mensual y alertas
-- =========================================================

-- Plantilla global del usuario (1 por usuario)
CREATE TABLE presupuesto_plantilla (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id            UUID NOT NULL UNIQUE REFERENCES usuarios(id) ON DELETE CASCADE,
    tipo_base             VARCHAR(20) NOT NULL,
    monto_fijo            NUMERIC(15, 2),
    fecha_creacion        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT presupuesto_plantilla_tipo_base_check
        CHECK (tipo_base IN ('INGRESOS_MES', 'MONTO_FIJO')),
    CONSTRAINT presupuesto_plantilla_monto_fijo_check
        CHECK (
            (tipo_base = 'MONTO_FIJO' AND monto_fijo IS NOT NULL AND monto_fijo > 0)
            OR (tipo_base = 'INGRESOS_MES' AND monto_fijo IS NULL)
        )
);

CREATE INDEX idx_presupuesto_plantilla_usuario ON presupuesto_plantilla(usuario_id);

-- Bolsillos definidos en la plantilla
CREATE TABLE bolsillo (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plantilla_id      UUID NOT NULL REFERENCES presupuesto_plantilla(id) ON DELETE CASCADE,
    nombre            VARCHAR(80) NOT NULL,
    porcentaje        NUMERIC(5, 2) NOT NULL,
    tipo              VARCHAR(30) NOT NULL,
    color             VARCHAR(7),
    orden             INT NOT NULL DEFAULT 0,
    CONSTRAINT bolsillo_porcentaje_check CHECK (porcentaje > 0 AND porcentaje <= 100),
    CONSTRAINT bolsillo_tipo_check
        CHECK (tipo IN ('GASTO', 'AHORRO_OBLIGATORIO', 'AHORRO_EMERGENCIA', 'OTRO'))
);

CREATE INDEX idx_bolsillo_plantilla ON bolsillo(plantilla_id);

-- Mapeo bolsillo <-> categorías default (para asignación automática)
CREATE TABLE bolsillo_categoria (
    bolsillo_id   UUID NOT NULL REFERENCES bolsillo(id) ON DELETE CASCADE,
    categoria     VARCHAR(40) NOT NULL,
    PRIMARY KEY (bolsillo_id, categoria)
);

-- Snapshot mensual del presupuesto (uno por usuario+mes)
CREATE TABLE presupuesto_mensual (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id         UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    anio               INT NOT NULL,
    mes                INT NOT NULL,
    base_calculada     NUMERIC(15, 2) NOT NULL,
    fecha_calculo      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT presupuesto_mensual_mes_check CHECK (mes BETWEEN 1 AND 12),
    CONSTRAINT presupuesto_mensual_unique UNIQUE (usuario_id, anio, mes)
);

CREATE INDEX idx_presupuesto_mensual_usuario ON presupuesto_mensual(usuario_id);

-- Bolsillos snapshot del mes (permiten override sin afectar plantilla)
CREATE TABLE bolsillo_mensual (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    presupuesto_mensual_id   UUID NOT NULL REFERENCES presupuesto_mensual(id) ON DELETE CASCADE,
    bolsillo_origen_id       UUID REFERENCES bolsillo(id) ON DELETE SET NULL,
    nombre                   VARCHAR(80) NOT NULL,
    tipo                     VARCHAR(30) NOT NULL,
    porcentaje               NUMERIC(5, 2) NOT NULL,
    monto_limite             NUMERIC(15, 2) NOT NULL,
    color                    VARCHAR(7),
    orden                    INT NOT NULL DEFAULT 0,
    CONSTRAINT bolsillo_mensual_porcentaje_check CHECK (porcentaje > 0 AND porcentaje <= 100),
    CONSTRAINT bolsillo_mensual_tipo_check
        CHECK (tipo IN ('GASTO', 'AHORRO_OBLIGATORIO', 'AHORRO_EMERGENCIA', 'OTRO'))
);

CREATE INDEX idx_bolsillo_mensual_presupuesto ON bolsillo_mensual(presupuesto_mensual_id);

-- Registro de alertas emitidas para evitar duplicados
CREATE TABLE alerta_presupuesto_emitida (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bolsillo_mensual_id  UUID NOT NULL REFERENCES bolsillo_mensual(id) ON DELETE CASCADE,
    nivel                VARCHAR(20) NOT NULL,
    fecha_emision        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT alerta_presupuesto_nivel_check
        CHECK (nivel IN ('ADVERTENCIA', 'EXCEDIDO')),
    CONSTRAINT alerta_presupuesto_unique UNIQUE (bolsillo_mensual_id, nivel)
);

-- Asignación manual de un gasto a un bolsillo (opcional, sobreescribe el mapping por categoría)
ALTER TABLE gastos ADD COLUMN bolsillo_id UUID REFERENCES bolsillo(id) ON DELETE SET NULL;
CREATE INDEX idx_gastos_bolsillo ON gastos(bolsillo_id);

COMMENT ON TABLE presupuesto_plantilla IS 'Plantilla de presupuesto global del usuario (50/15/25/10 por defecto)';
COMMENT ON TABLE bolsillo IS 'Bolsillo de la plantilla con porcentaje del 100% configurado';
COMMENT ON TABLE bolsillo_categoria IS 'Mapeo de categorías default que se asignan automáticamente a un bolsillo';
COMMENT ON TABLE presupuesto_mensual IS 'Snapshot del presupuesto aplicado a un mes específico';
COMMENT ON TABLE bolsillo_mensual IS 'Bolsillo snapshot del mes; permite overrides puntuales sin tocar la plantilla';
COMMENT ON TABLE alerta_presupuesto_emitida IS 'Registro idempotente de alertas (80% / 100%) ya enviadas al usuario';
