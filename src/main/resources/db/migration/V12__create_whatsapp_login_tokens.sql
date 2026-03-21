CREATE TABLE whatsapp_login_tokens (
    id              UUID PRIMARY KEY,
    token           VARCHAR(255) NOT NULL UNIQUE,
    numero_whatsapp VARCHAR(20)  NOT NULL,
    usado           BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_expiracion TIMESTAMP   NOT NULL,
    fecha_creacion   TIMESTAMP   NOT NULL
);

CREATE INDEX idx_whatsapp_login_tokens_token ON whatsapp_login_tokens (token);
CREATE INDEX idx_whatsapp_login_tokens_expiracion ON whatsapp_login_tokens (fecha_expiracion);
