-- Agrega la columna dos_factores_activado que fue añadida a UsuarioEntity
-- pero no existía en la base de datos (causa del error de login OAuth)
ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS dos_factores_activado BOOLEAN NOT NULL DEFAULT false;
