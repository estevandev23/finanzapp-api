package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.Gasto;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.GastoEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.GastoMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.GastoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GastoRepositoryAdapter")
class GastoRepositoryAdapterTest {

    @Mock
    private GastoJpaRepository jpaRepository;

    private GastoRepositoryAdapter adapter;

    private UUID gastoId;
    private GastoEntity entityExistente;

    @BeforeEach
    void setUp() {
        adapter = new GastoRepositoryAdapter(jpaRepository, new GastoMapper());
        gastoId = UUID.randomUUID();
        entityExistente = GastoEntity.builder()
                .id(gastoId)
                .usuarioId(UUID.randomUUID())
                .monto(new BigDecimal("50000"))
                .categoria(CategoriaGasto.COMIDA)
                .mesFacturacion(LocalDate.now().withDayOfMonth(1))
                .bolsilloId(null)
                .descripcion("Mercado")
                .fecha(LocalDate.now())
                .fechaCreacion(LocalDateTime.now().minusDays(5))
                .fechaActualizacion(LocalDateTime.now().minusDays(5))
                .metodosPago(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("al actualizar un gasto existente persiste el bolsilloId asignado")
    void savePersisteBolsilloIdAlActualizar() {
        UUID bolsilloId = UUID.randomUUID();
        when(jpaRepository.findById(gastoId)).thenReturn(Optional.of(entityExistente));
        when(jpaRepository.save(any(GastoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Gasto gasto = new GastoMapper().toDomain(entityExistente);
        gasto.setBolsilloId(bolsilloId);

        Gasto resultado = adapter.save(gasto);

        ArgumentCaptor<GastoEntity> captor = ArgumentCaptor.forClass(GastoEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertEquals(bolsilloId, captor.getValue().getBolsilloId());
        assertEquals(bolsilloId, resultado.getBolsilloId());
    }
}
