-- Agregar categoria_personalizada_id a deudas para soportar categorias personalizadas
ALTER TABLE deudas ADD COLUMN categoria_personalizada_id UUID REFERENCES categorias_personalizadas(id);
