-- Fase 2: Tarjetas de crédito.
-- Permite registrar tarjetas con día de corte y día de pago configurables. Los gastos
-- con método TARJETA_CREDITO se enlazan a una tarjeta y se contabilizan en el mes de
-- facturación (calculado a partir del día de corte), no en el mes de la compra.

CREATE TABLE tarjetas_credito (
    id                     UUID PRIMARY KEY,
    usuario_id             UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nombre                 VARCHAR(100) NOT NULL,
    banco                  VARCHAR(100),
    ultimos_cuatro         VARCHAR(4),
    cupo_total             DECIMAL(15, 2) NOT NULL,
    cupo_usado             DECIMAL(15, 2) NOT NULL DEFAULT 0,
    dia_corte              INT NOT NULL,
    dia_pago               INT NOT NULL,
    color                  VARCHAR(7),
    estado                 VARCHAR(15) NOT NULL DEFAULT 'ACTIVA',
    fecha_creacion         TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion    TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT tarjetas_credito_estado_check
        CHECK (estado IN ('ACTIVA', 'BLOQUEADA', 'CANCELADA')),
    CONSTRAINT tarjetas_credito_cupo_total_positivo
        CHECK (cupo_total > 0),
    CONSTRAINT tarjetas_credito_cupo_usado_no_negativo
        CHECK (cupo_usado >= 0),
    CONSTRAINT tarjetas_credito_dia_corte_rango
        CHECK (dia_corte BETWEEN 1 AND 31),
    CONSTRAINT tarjetas_credito_dia_pago_rango
        CHECK (dia_pago BETWEEN 1 AND 31),
    CONSTRAINT tarjetas_credito_ultimos_cuatro_formato
        CHECK (ultimos_cuatro IS NULL OR ultimos_cuatro ~ '^[0-9]{4}$')
);

CREATE INDEX idx_tarjetas_credito_usuario_id ON tarjetas_credito(usuario_id);
CREATE INDEX idx_tarjetas_credito_usuario_estado ON tarjetas_credito(usuario_id, estado);

-- Permite TARJETA_CREDITO en métodos de pago de ingresos
ALTER TABLE ingresos DROP CONSTRAINT IF EXISTS ingresos_metodo_pago_check;
ALTER TABLE ingresos ADD CONSTRAINT ingresos_metodo_pago_check
    CHECK (metodo_pago IN ('EFECTIVO', 'NEQUI', 'BANCOLOMBIA', 'TARJETA_CREDITO', 'OTRO'));

-- Permite TARJETA_CREDITO en métodos de pago de gastos
ALTER TABLE gasto_metodo_pago DROP CONSTRAINT IF EXISTS gasto_metodo_pago_metodo_check;
ALTER TABLE gasto_metodo_pago ADD CONSTRAINT gasto_metodo_pago_metodo_check
    CHECK (metodo IN ('EFECTIVO', 'NEQUI', 'BANCOLOMBIA', 'TARJETA_CREDITO', 'OTRO'));

-- Enlaza el gasto a la tarjeta usada (opcional, solo para gastos con tarjeta de crédito)
ALTER TABLE gastos ADD COLUMN tarjeta_id UUID REFERENCES tarjetas_credito(id) ON DELETE SET NULL;

-- Primer día del mes en que el gasto se contabiliza (mes del extracto).
-- Para gastos sin tarjeta es igual al primer día del mes de la fecha del gasto.
-- Para gastos con tarjeta se calcula según el día de corte.
ALTER TABLE gastos ADD COLUMN mes_facturacion DATE;

-- Backfill: para gastos existentes, mes_facturacion = primer día del mes de la fecha
UPDATE gastos SET mes_facturacion = date_trunc('month', fecha)::date WHERE mes_facturacion IS NULL;

ALTER TABLE gastos ALTER COLUMN mes_facturacion SET NOT NULL;

CREATE INDEX idx_gastos_tarjeta_id ON gastos(tarjeta_id);
CREATE INDEX idx_gastos_usuario_mes_facturacion ON gastos(usuario_id, mes_facturacion);

-- Tabla de abonos a la tarjeta: registra pagos del usuario para liberar cupo.
-- No genera un Gasto separado (los gastos individuales ya están contabilizados en su
-- mes de facturación); solo libera cupo de la tarjeta.
CREATE TABLE abonos_tarjeta_credito (
    id                  UUID PRIMARY KEY,
    tarjeta_id          UUID NOT NULL REFERENCES tarjetas_credito(id) ON DELETE CASCADE,
    monto               DECIMAL(15, 2) NOT NULL,
    descripcion         VARCHAR(255),
    fecha_abono         DATE NOT NULL,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT abonos_tarjeta_credito_monto_positivo CHECK (monto > 0)
);

CREATE INDEX idx_abonos_tarjeta_credito_tarjeta ON abonos_tarjeta_credito(tarjeta_id, fecha_abono DESC);
