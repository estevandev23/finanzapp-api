-- =========================================================
-- V18 - Unificación recurrencias + presupuesto
-- 1) Nueva base de presupuesto calculada desde ingresos recurrentes.
-- 2) Asociación opcional de una recurrencia de gasto con un bolsillo del presupuesto.
-- =========================================================
-- 1) Permitir tipo_base = 'INGRESOS_RECURRENTES' (la base se calcula sumando los
--    ingresos recurrentes activos del usuario, sin requerir monto_fijo).
ALTER TABLE presupuesto_plantilla DROP CONSTRAINT IF EXISTS presupuesto_plantilla_tipo_base_check;
ALTER TABLE presupuesto_plantilla
ADD CONSTRAINT presupuesto_plantilla_tipo_base_check CHECK (
    tipo_base IN (
      'INGRESOS_MES',
      'MONTO_FIJO',
      'INGRESOS_RECURRENTES'
    )
  );
ALTER TABLE presupuesto_plantilla DROP CONSTRAINT IF EXISTS presupuesto_plantilla_monto_fijo_check;
ALTER TABLE presupuesto_plantilla
ADD CONSTRAINT presupuesto_plantilla_monto_fijo_check CHECK (
    (
      tipo_base = 'MONTO_FIJO'
      AND monto_fijo IS NOT NULL
      AND monto_fijo > 0
    )
    OR (
      tipo_base IN ('INGRESOS_MES', 'INGRESOS_RECURRENTES')
      AND monto_fijo IS NULL
    )
  );
-- 2) Bolsillo asociado a una recurrencia de gasto. Al confirmar la recurrencia, el
--    gasto generado hereda este bolsillo para descontar de su límite de presupuesto.
DO $$ BEGIN IF NOT EXISTS (
  SELECT 1
  FROM information_schema.columns
  WHERE table_name = 'recurrencias'
    AND column_name = 'bolsillo_id'
) THEN
ALTER TABLE recurrencias
ADD COLUMN bolsillo_id UUID REFERENCES bolsillo(id) ON DELETE
SET NULL;
END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_recurrencias_bolsillo_id ON recurrencias(bolsillo_id);