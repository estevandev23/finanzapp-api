package com.finanzapp.application.service;

import com.finanzapp.domain.model.CategoriaGasto;
import com.finanzapp.domain.model.Gasto;
import com.finanzapp.domain.port.in.TarjetaCreditoUseCase;
import com.finanzapp.domain.port.out.AbonoDeudaRepositoryPort;
import com.finanzapp.domain.port.out.DeudaRepositoryPort;
import com.finanzapp.domain.port.out.GastoRepositoryPort;
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
@DisplayName("GastoService")
class GastoServiceTest {

    @Mock
    private GastoRepositoryPort gastoRepository;
    @Mock
    private DeudaRepositoryPort deudaRepository;
    @Mock
    private AbonoDeudaRepositoryPort abonoDeudaRepository;
    @Mock
    private TarjetaCreditoUseCase tarjetaUseCase;

    private GastoService service;

    private UUID gastoId;
    private Gasto gastoExistente;

    @BeforeEach
    void setUp() {
        service = new GastoService(gastoRepository, deudaRepository, abonoDeudaRepository, tarjetaUseCase);
        gastoId = UUID.randomUUID();
        gastoExistente = Gasto.builder()
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
                .build();
    }

    @Test
    @DisplayName("actualizar asigna el bolsillo enviado y lo persiste")
    void actualizarAsignaBolsillo() {
        UUID bolsilloId = UUID.randomUUID();
        when(gastoRepository.findById(gastoId)).thenReturn(Optional.of(gastoExistente));
        when(gastoRepository.save(any(Gasto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(abonoDeudaRepository.findByGastoId(gastoId)).thenReturn(Optional.empty());

        Gasto datos = Gasto.builder().bolsilloId(bolsilloId).build();
        Gasto resultado = service.actualizar(gastoId, datos);

        ArgumentCaptor<Gasto> captor = ArgumentCaptor.forClass(Gasto.class);
        verify(gastoRepository).save(captor.capture());
        assertEquals(bolsilloId, captor.getValue().getBolsilloId());
        assertEquals(bolsilloId, resultado.getBolsilloId());
    }

    @Test
    @DisplayName("actualizar conserva el bolsillo actual cuando no se envia uno nuevo")
    void actualizarConservaBolsilloSiNoSeEnvia() {
        UUID bolsilloOriginal = UUID.randomUUID();
        gastoExistente.setBolsilloId(bolsilloOriginal);
        when(gastoRepository.findById(gastoId)).thenReturn(Optional.of(gastoExistente));
        when(gastoRepository.save(any(Gasto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(abonoDeudaRepository.findByGastoId(gastoId)).thenReturn(Optional.empty());

        Gasto datos = Gasto.builder().descripcion("Mercado semanal").build();
        Gasto resultado = service.actualizar(gastoId, datos);

        assertEquals(bolsilloOriginal, resultado.getBolsilloId());
        assertEquals("Mercado semanal", resultado.getDescripcion());
    }
}
