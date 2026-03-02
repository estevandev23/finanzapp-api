-- Actualiza los check constraints de categoria para incluir el nuevo valor ABONO.
-- Hibernate genera estos constraints automáticamente al hacer ddl-auto: create,
-- pero con Flyway+validate no se actualizan solos al agregar valores al enum.

ALTER TABLE gastos DROP CONSTRAINT IF EXISTS gastos_categoria_check;
ALTER TABLE gastos ADD CONSTRAINT gastos_categoria_check
    CHECK (categoria IN (
        'COMIDA','PAREJA','COMPRAS','TRANSPORTE','SERVICIOS',
        'ENTRETENIMIENTO','SALUD','EDUCACION','OTROS','ABONO'
    ));

ALTER TABLE ingresos DROP CONSTRAINT IF EXISTS ingresos_categoria_check;
ALTER TABLE ingresos ADD CONSTRAINT ingresos_categoria_check
    CHECK (categoria IN (
        'TRABAJO_PRINCIPAL','TRABAJO_EXTRA','GANANCIAS_ADICIONALES',
        'INVERSIONES','OTROS','ABONO'
    ));
