package com.finanzapp.application.service;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.FrecuenciaRecurrencia;
import com.finanzapp.domain.model.Gasto;
import com.finanzapp.domain.model.MetodoPago;
import com.finanzapp.domain.model.Recurrencia;
import com.finanzapp.domain.model.TipoRecurrencia;
import com.finanzapp.domain.port.in.DeudaUseCase;
import com.finanzapp.domain.port.in.GastoUseCase;
import com.finanzapp.domain.port.in.IngresoUseCase;
import com.finanzapp.domain.port.out.RecurrenciaRepositoryPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecurrenciaService")
class RecurrenciaServiceTest {

    @Mock
    private RecurrenciaRepositoryPort recurrenciaRepository;
    @Mock
    private IngresoUseCase ingresoUseCase;
    @Mock
    private GastoUseCase gastoUseCase;
    @Mock
    private DeudaUseCase deudaUseCase;

    private RecurrenciaService service;

    private UUID recurrenciaId;
    private Recurrencia recurrenciaExistente;

    @BeforeEach
    void setUp() {
        service = new RecurrenciaService(recurrenciaRepository, ingresoUseCase, gastoUseCase, deudaUseCase);
        recurrenciaId = UUID.randomUUID();
        recurrenciaExistente = Recurrencia.builder()
                .id(recurrenciaId)
                .usuarioId(UUID.randomUUID())
                .tipo(TipoRecurrencia.GASTO)
                .frecuencia(FrecuenciaRecurrencia.MENSUAL)
                .descripcion("Arriendo")
                .monto(new BigDecimal("1500000"))
                .categoriaGasto(CategoriaGasto.SERVICIOS)
                .metodoPago(MetodoPago.EFECTIVO)
                .bolsilloId(null)
                .diaVencimiento(5)
                .proximaFecha(LocalDate.now().plusDays(10))
                .activa(true)
                .fechaCreacion(LocalDateTime.now().minusDays(30))
                .fechaActualizacion(LocalDateTime.now().minusDays(30))
                .build();
    }

    @Test
    @DisplayName("actualizar asigna el bolsillo enviado y lo persiste")
    void actualizarAsignaBolsillo() {
        UUID bolsilloId = UUID.randomUUID();
        when(recurrenciaRepository.findById(recurrenciaId)).thenReturn(Optional.of(recurrenciaExistente));
        when(recurrenciaRepository.save(any(Recurrencia.class))).thenAnswer(inv -> inv.getArgument(0));

        Recurrencia datos = Recurrencia.builder().bolsilloId(bolsilloId).build();
        Recurrencia resultado = service.actualizar(recurrenciaId, datos);

        ArgumentCaptor<Recurrencia> captor = ArgumentCaptor.forClass(Recurrencia.class);
        verify(recurrenciaRepository).save(captor.capture());
        assertEquals(bolsilloId, captor.getValue().getBolsilloId());
        assertEquals(bolsilloId, resultado.getBolsilloId());
    }

    @Test
    @DisplayName("actualizar conserva el bolsillo actual cuando no se envia uno nuevo")
    void actualizarConservaBolsilloSiNoSeEnvia() {
        UUID bolsilloOriginal = UUID.randomUUID();
        recurrenciaExistente.setBolsilloId(bolsilloOriginal);
        when(recurrenciaRepository.findById(recurrenciaId)).thenReturn(Optional.of(recurrenciaExistente));
        when(recurrenciaRepository.save(any(Recurrencia.class))).thenAnswer(inv -> inv.getArgument(0));

        Recurrencia datos = Recurrencia.builder().descripcion("Arriendo actualizado").build();
        Recurrencia resultado = service.actualizar(recurrenciaId, datos);

        assertEquals(bolsilloOriginal, resultado.getBolsilloId());
        assertEquals("Arriendo actualizado", resultado.getDescripcion());
    }

    @Test
    @DisplayName("confirmar genera el gasto con el mismo bolsillo de la recurrencia")
    void confirmarGeneraGastoConBolsilloDeRecurrencia() {
        UUID bolsilloId = UUID.randomUUID();
        recurrenciaExistente.setBolsilloId(bolsilloId);
        // La recurrencia debe estar vencida (o por vencer dentro de la ventana) para poder confirmarse.
        recurrenciaExistente.setProximaFecha(LocalDate.now());
        when(recurrenciaRepository.findById(recurrenciaId)).thenReturn(Optional.of(recurrenciaExistente));
        when(recurrenciaRepository.save(any(Recurrencia.class))).thenAnswer(inv -> inv.getArgument(0));
        when(gastoUseCase.registrar(any(Gasto.class)))
                .thenAnswer(inv -> {
                    Gasto gasto = inv.getArgument(0);
                    gasto.setId(UUID.randomUUID());
                    return gasto;
                });

        service.confirmar(recurrenciaId, LocalDate.now());

        ArgumentCaptor<Gasto> captor = ArgumentCaptor.forClass(Gasto.class);
        verify(gastoUseCase).registrar(captor.capture());
        assertEquals(bolsilloId, captor.getValue().getBolsilloId());
    }
}
