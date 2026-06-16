package com.finanzapp.infrastructure.adapter.out.persistence;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.FrecuenciaRecurrencia;
import com.finanzapp.domain.model.MetodoPago;
import com.finanzapp.domain.model.Recurrencia;
import com.finanzapp.domain.model.TipoRecurrencia;
import com.finanzapp.infrastructure.adapter.out.persistence.entity.RecurrenciaEntity;
import com.finanzapp.infrastructure.adapter.out.persistence.mapper.RecurrenciaMapper;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.RecurrenciaJpaRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecurrenciaRepositoryAdapter")
class RecurrenciaRepositoryAdapterTest {

    @Mock
    private RecurrenciaJpaRepository jpaRepository;

    private RecurrenciaRepositoryAdapter adapter;

    private UUID recurrenciaId;
    private RecurrenciaEntity entityExistente;

    @BeforeEach
    void setUp() {
        adapter = new RecurrenciaRepositoryAdapter(jpaRepository, new RecurrenciaMapper());
        recurrenciaId = UUID.randomUUID();
        entityExistente = RecurrenciaEntity.builder()
                .id(recurrenciaId)
                .usuarioId(UUID.randomUUID())
                .tipo(TipoRecurrencia.GASTO)
                .frecuencia(FrecuenciaRecurrencia.MENSUAL)
                .descripcion("Arriendo")
                .monto(new BigDecimal("1500000"))
                .categoriaGasto(CategoriaGasto.SERVICIOS)
                .metodoPago(MetodoPago.EFECTIVO)
                .bolsilloId(null)
                .tarjetaId(null)
                .diaVencimiento(5)
                .proximaFecha(LocalDate.now().plusDays(10))
                .activa(true)
                .fechaCreacion(LocalDateTime.now().minusDays(30))
                .fechaActualizacion(LocalDateTime.now().minusDays(30))
                .build();
    }

    private Recurrencia dominioDesde(RecurrenciaEntity entity) {
        return new RecurrenciaMapper().toDomain(entity);
    }

    @Test
    @DisplayName("al actualizar una recurrencia existente persiste el bolsilloId asignado")
    void savePersisteBolsilloIdAlActualizar() {
        UUID bolsilloId = UUID.randomUUID();
        when(jpaRepository.findById(recurrenciaId)).thenReturn(Optional.of(entityExistente));
        when(jpaRepository.save(any(RecurrenciaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Recurrencia recurrencia = dominioDesde(entityExistente);
        recurrencia.setBolsilloId(bolsilloId);

        Recurrencia resultado = adapter.save(recurrencia);

        ArgumentCaptor<RecurrenciaEntity> captor = ArgumentCaptor.forClass(RecurrenciaEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertEquals(bolsilloId, captor.getValue().getBolsilloId());
        assertEquals(bolsilloId, resultado.getBolsilloId());
    }

    @Test
    @DisplayName("al actualizar una recurrencia existente persiste el tarjetaId asignado")
    void savePersisteTarjetaIdAlActualizar() {
        UUID tarjetaId = UUID.randomUUID();
        when(jpaRepository.findById(recurrenciaId)).thenReturn(Optional.of(entityExistente));
        when(jpaRepository.save(any(RecurrenciaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Recurrencia recurrencia = dominioDesde(entityExistente);
        recurrencia.setMetodoPago(MetodoPago.TARJETA_CREDITO);
        recurrencia.setTarjetaId(tarjetaId);

        Recurrencia resultado = adapter.save(recurrencia);

        ArgumentCaptor<RecurrenciaEntity> captor = ArgumentCaptor.forClass(RecurrenciaEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertEquals(tarjetaId, captor.getValue().getTarjetaId());
        assertEquals(tarjetaId, resultado.getTarjetaId());
    }

    @Test
    @DisplayName("al actualizar refleja fielmente un bolsillo nulo del dominio")
    void saveReflejaBolsilloNuloAlActualizar() {
        entityExistente.setBolsilloId(UUID.randomUUID());
        when(jpaRepository.findById(recurrenciaId)).thenReturn(Optional.of(entityExistente));
        when(jpaRepository.save(any(RecurrenciaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Recurrencia recurrencia = dominioDesde(entityExistente);
        recurrencia.setBolsilloId(null);

        adapter.save(recurrencia);

        ArgumentCaptor<RecurrenciaEntity> captor = ArgumentCaptor.forClass(RecurrenciaEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertNull(captor.getValue().getBolsilloId());
    }
}
