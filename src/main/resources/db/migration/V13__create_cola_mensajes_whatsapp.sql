CREATE TABLE IF NOT EXISTS cola_mensajes_whatsapp (
    id BIGSERIAL PRIMARY KEY,
    numero_telefono VARCHAR(20) NOT NULL,
    texto_mensaje TEXT NOT NULL,
    nombre_contacto VARCHAR(100),
    message_id VARCHAR(100),
    es_audio BOOLEAN NOT NULL DEFAULT FALSE,
    recibido_en TIMESTAMP NOT NULL DEFAULT NOW(),
    procesado BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_cola_telefono_procesado ON cola_mensajes_whatsapp(numero_telefono, procesado, recibido_en);
