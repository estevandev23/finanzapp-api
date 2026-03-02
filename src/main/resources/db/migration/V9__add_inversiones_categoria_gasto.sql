-- Actualiza el check constraint de gastos para incluir el valor INVERSIONES,
-- necesario para registrar automáticamente el gasto al crear una inversión.

ALTER TABLE gastos DROP CONSTRAINT IF EXISTS gastos_categoria_check;
ALTER TABLE gastos ADD CONSTRAINT gastos_categoria_check
    CHECK (categoria IN (
        'COMIDA','PAREJA','COMPRAS','TRANSPORTE','SERVICIOS',
        'ENTRETENIMIENTO','SALUD','EDUCACION','INVERSIONES','OTROS','ABONO'
    ));
