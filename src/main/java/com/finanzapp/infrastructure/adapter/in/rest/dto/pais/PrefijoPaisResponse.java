package com.finanzapp.infrastructure.adapter.in.rest.dto.pais;

import java.util.UUID;

public record PrefijoPaisResponse(
        UUID id,
        String nombre,
        String codigoIso,
        String prefijoTelefono,
        String banderaEmoji
) {}
