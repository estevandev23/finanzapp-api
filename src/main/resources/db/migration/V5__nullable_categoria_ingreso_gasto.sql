-- Permite categoria NULL cuando se usa una categoria personalizada.
-- La columna debe ser nullable ya que el usuario puede indicar
-- categoriaPersonalizadaId en lugar de una categoria predefinida.

ALTER TABLE ingresos
    ALTER COLUMN categoria DROP NOT NULL;

ALTER TABLE gastos
    ALTER COLUMN categoria DROP NOT NULL;
