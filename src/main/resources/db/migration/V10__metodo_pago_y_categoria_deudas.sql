-- Agrega soporte para metodo de pago en ingresos y gastos.
-- Los ingresos tienen un solo metodo. Los gastos pueden dividirse en multiples metodos.

-- Columna metodo_pago en ingresos (un solo metodo por ingreso)
ALTER TABLE ingresos ADD COLUMN metodo_pago VARCHAR(20) NOT NULL DEFAULT 'EFECTIVO';
ALTER TABLE ingresos ADD CONSTRAINT ingresos_metodo_pago_check
    CHECK (metodo_pago IN ('EFECTIVO', 'NEQUI', 'BANCOLOMBIA', 'OTRO'));

-- Tabla para el desglose de metodos de pago por gasto
CREATE TABLE gasto_metodo_pago (
    id UUID PRIMARY KEY,
    gasto_id UUID NOT NULL REFERENCES gastos(id) ON DELETE CASCADE,
    metodo VARCHAR(20) NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    CONSTRAINT gasto_metodo_pago_metodo_check CHECK (metodo IN ('EFECTIVO', 'NEQUI', 'BANCOLOMBIA', 'OTRO')),
    CONSTRAINT gasto_metodo_pago_monto_positivo CHECK (monto > 0)
);

CREATE INDEX idx_gasto_metodo_pago_gasto_id ON gasto_metodo_pago(gasto_id);
CREATE INDEX idx_gasto_metodo_pago_metodo ON gasto_metodo_pago(metodo);

-- Migrar gastos existentes: crear un registro por cada gasto con metodo EFECTIVO
INSERT INTO gasto_metodo_pago (id, gasto_id, metodo, monto)
SELECT gen_random_uuid(), id, 'EFECTIVO', monto FROM gastos;

-- Columna categoria en deudas (para categorizar prestamos)
ALTER TABLE deudas ADD COLUMN categoria VARCHAR(100);
