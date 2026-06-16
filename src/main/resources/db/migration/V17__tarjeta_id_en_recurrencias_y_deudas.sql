-- Fase 2.1: Asociación opcional de recurrencias y deudas con tarjetas de crédito.
-- Permite registrar gastos recurrentes y deudas pagadas con tarjeta. Estos consumen
-- cupo de la tarjeta pero no afectan el dinero disponible del usuario; el dinero
-- realmente sale al hacer un abono a la tarjeta.

-- Fix: V14 creó el constraint sin TARJETA_CREDITO, lo que impedía registrar
-- recurrencias pagadas con tarjeta. Se actualiza para incluirlo.
ALTER TABLE recurrencias DROP CONSTRAINT IF EXISTS recurrencias_metodo_pago_check;
ALTER TABLE recurrencias ADD CONSTRAINT recurrencias_metodo_pago_check
    CHECK (metodo_pago IN ('EFECTIVO', 'NEQUI', 'BANCOLOMBIA', 'TARJETA_CREDITO', 'OTRO'));

-- Tarjeta asociada (opcional, solo cuando el método de pago es TARJETA_CREDITO).
ALTER TABLE recurrencias
    ADD COLUMN tarjeta_id UUID REFERENCES tarjetas_credito(id) ON DELETE SET NULL;

-- Tarjeta asociada a la deuda (opcional). Aplica para deudas adquiridas con tarjeta
-- de crédito: el saldo restante consume cupo de la tarjeta hasta que se abona.
ALTER TABLE deudas
    ADD COLUMN tarjeta_id UUID REFERENCES tarjetas_credito(id) ON DELETE SET NULL;

CREATE INDEX idx_recurrencias_tarjeta_id ON recurrencias(tarjeta_id);
CREATE INDEX idx_deudas_tarjeta_id ON deudas(tarjeta_id);
