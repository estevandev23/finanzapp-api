-- Recurrencias: plantillas reutilizables para generar ingresos o gastos en fechas previsibles
-- (sueldo mensual, prima semestral, suscripciones, etc.). No son registros reales hasta que el
-- usuario las confirma con "ya me pagaron" o "marcar como pagado".

CREATE TABLE recurrencias (
    id                          UUID PRIMARY KEY,
    usuario_id                  UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    tipo                        VARCHAR(10) NOT NULL,
    frecuencia                  VARCHAR(15) NOT NULL,
    descripcion                 VARCHAR(255) NOT NULL,
    monto                       DECIMAL(15, 2) NOT NULL,
    categoria_ingreso           VARCHAR(30),
    categoria_gasto             VARCHAR(30),
    categoria_personalizada_id  UUID REFERENCES categorias_personalizadas(id) ON DELETE SET NULL,
    metodo_pago                 VARCHAR(20) NOT NULL DEFAULT 'EFECTIVO',
    dia_vencimiento             INT NOT NULL,
    mes_referencia              INT,
    proxima_fecha               DATE NOT NULL,
    ultima_confirmacion_fecha   DATE,
    activa                      BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion              TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion         TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT recurrencias_tipo_check
        CHECK (tipo IN ('INGRESO', 'GASTO')),
    CONSTRAINT recurrencias_frecuencia_check
        CHECK (frecuencia IN ('MENSUAL', 'QUINCENAL', 'SEMESTRAL', 'ANUAL')),
    CONSTRAINT recurrencias_monto_positivo
        CHECK (monto > 0),
    CONSTRAINT recurrencias_dia_vencimiento_rango
        CHECK (dia_vencimiento BETWEEN 1 AND 31),
    CONSTRAINT recurrencias_mes_referencia_rango
        CHECK (mes_referencia IS NULL OR mes_referencia BETWEEN 1 AND 12),
    CONSTRAINT recurrencias_metodo_pago_check
        CHECK (metodo_pago IN ('EFECTIVO', 'NEQUI', 'BANCOLOMBIA', 'OTRO')),
    CONSTRAINT recurrencias_categoria_ingreso_check
        CHECK (categoria_ingreso IS NULL OR categoria_ingreso IN (
            'TRABAJO_PRINCIPAL', 'TRABAJO_EXTRA', 'GANANCIAS_ADICIONALES',
            'INVERSIONES', 'OTROS', 'ABONO'
        )),
    CONSTRAINT recurrencias_categoria_gasto_check
        CHECK (categoria_gasto IS NULL OR categoria_gasto IN (
            'COMIDA', 'PAREJA', 'COMPRAS', 'TRANSPORTE', 'SERVICIOS',
            'ENTRETENIMIENTO', 'SALUD', 'EDUCACION', 'INVERSIONES', 'OTROS', 'ABONO'
        )),
    CONSTRAINT recurrencias_categoria_coherente
        CHECK (
            (tipo = 'INGRESO' AND categoria_gasto IS NULL) OR
            (tipo = 'GASTO'   AND categoria_ingreso IS NULL)
        )
);

CREATE INDEX idx_recurrencias_usuario_id          ON recurrencias(usuario_id);
CREATE INDEX idx_recurrencias_activa_proxima      ON recurrencias(activa, proxima_fecha);
CREATE INDEX idx_recurrencias_usuario_tipo_activa ON recurrencias(usuario_id, tipo, activa);
