CREATE TABLE inversiones (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    gasto_id UUID REFERENCES gastos(id) ON DELETE SET NULL,
    ingreso_id UUID REFERENCES ingresos(id) ON DELETE SET NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    monto DECIMAL(15, 2) NOT NULL,
    retorno_esperado DECIMAL(15, 2),
    retorno_real DECIMAL(15, 2),
    estado VARCHAR(50) NOT NULL DEFAULT 'ACTIVA',
    fecha_inversion DATE NOT NULL,
    fecha_retorno DATE,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP NOT NULL
);

CREATE INDEX idx_inversiones_usuario_id ON inversiones(usuario_id);
CREATE INDEX idx_inversiones_estado ON inversiones(estado);
