-- Permite asociar un gasto directamente a una deuda (el gasto representa el abono)
ALTER TABLE gastos ADD COLUMN deuda_id UUID REFERENCES deudas(id);

-- Permite asociar un ingreso directamente a un prestamo (el ingreso representa el cobro del abono)
ALTER TABLE ingresos ADD COLUMN prestamo_id UUID REFERENCES deudas(id);

-- Permite rastrear el gasto o ingreso que generó el abono de forma bidireccional
ALTER TABLE abonos_deuda ADD COLUMN gasto_id UUID REFERENCES gastos(id);
ALTER TABLE abonos_deuda ADD COLUMN ingreso_id UUID REFERENCES ingresos(id);
