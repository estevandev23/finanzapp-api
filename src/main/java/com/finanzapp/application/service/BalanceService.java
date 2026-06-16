package com.finanzapp.application.service;

import com.finanzapp.domain.model.Balance;
import com.finanzapp.domain.model.MetodoPago;
import com.finanzapp.domain.model.TipoDeuda;
import com.finanzapp.domain.port.in.BalanceUseCase;
import com.finanzapp.domain.port.out.AhorroRepositoryPort;
import com.finanzapp.domain.port.out.DeudaRepositoryPort;
import com.finanzapp.domain.port.out.GastoRepositoryPort;
import com.finanzapp.domain.port.out.IngresoRepositoryPort;
import com.finanzapp.infrastructure.adapter.out.persistence.repository.GastoMetodoPagoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BalanceService implements BalanceUseCase {

    private final IngresoRepositoryPort ingresoRepository;
    private final GastoRepositoryPort gastoRepository;
    private final AhorroRepositoryPort ahorroRepository;
    private final DeudaRepositoryPort deudaRepository;
    private final GastoMetodoPagoJpaRepository gastoMetodoPagoRepository;

    @Override
    public Balance obtenerBalanceGeneral(UUID usuarioId) {
        BigDecimal totalIngresos = obtenerValorOCero(ingresoRepository.sumMontoByUsuarioId(usuarioId));
        BigDecimal totalGastos = obtenerValorOCero(gastoRepository.sumMontoByUsuarioId(usuarioId));
        BigDecimal totalAhorros = obtenerValorOCero(ahorroRepository.sumMontoByUsuarioId(usuarioId));
        BigDecimal ahorrosDesdeIngresos = obtenerValorOCero(ingresoRepository.sumMontoAhorroByUsuarioId(usuarioId));
        BigDecimal totalDeudas = obtenerValorOCero(
                deudaRepository.sumMontoRestanteByUsuarioIdAndTipo(usuarioId, TipoDeuda.DEUDA));
        BigDecimal totalPrestamos = obtenerValorOCero(
                deudaRepository.sumMontoRestanteByUsuarioIdAndTipo(usuarioId, TipoDeuda.PRESTAMO));
        BigDecimal gastosEnTarjeta = obtenerValorOCero(gastoRepository.sumMontoConTarjetaByUsuarioId(usuarioId));
        BigDecimal deudasEnTarjeta = obtenerValorOCero(
                deudaRepository.sumMontoRestanteConTarjetaByUsuarioIdAndTipo(usuarioId, TipoDeuda.DEUDA));

        Balance balance = Balance.calcular(totalIngresos, totalGastos, totalAhorros, ahorrosDesdeIngresos,
                totalDeudas, totalPrestamos, gastosEnTarjeta, deudasEnTarjeta);
        balance.setUsuarioId(usuarioId);
        return balance;
    }

    @Override
    public Balance obtenerBalancePorPeriodo(UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        BigDecimal totalIngresos = obtenerValorOCero(
                ingresoRepository.sumMontoByUsuarioIdAndFechaBetween(usuarioId, fechaInicio, fechaFin));
        BigDecimal totalGastos = obtenerValorOCero(
                gastoRepository.sumMontoByUsuarioIdAndFechaBetween(usuarioId, fechaInicio, fechaFin));
        BigDecimal totalAhorros = obtenerValorOCero(
                ahorroRepository.sumMontoByUsuarioIdAndFechaBetween(usuarioId, fechaInicio, fechaFin));
        BigDecimal ahorrosDesdeIngresos = obtenerValorOCero(
                ingresoRepository.sumMontoAhorroByUsuarioIdAndFechaBetween(usuarioId, fechaInicio, fechaFin));
        BigDecimal gastosEnTarjeta = obtenerValorOCero(
                gastoRepository.sumMontoConTarjetaByUsuarioIdAndFechaBetween(usuarioId, fechaInicio, fechaFin));

        Balance balance = Balance.calcular(totalIngresos, totalGastos, totalAhorros, ahorrosDesdeIngresos,
                BigDecimal.ZERO, BigDecimal.ZERO, gastosEnTarjeta, BigDecimal.ZERO);
        balance.setUsuarioId(usuarioId);
        return balance;
    }

    @Override
    public Map<String, BigDecimal[]> obtenerBalancePorMetodoPago(UUID usuarioId) {
        Map<String, BigDecimal[]> resultado = new LinkedHashMap<>();
        for (MetodoPago metodo : MetodoPago.values()) {
            resultado.put(metodo.name(), new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }

        List<Object[]> ingresosPorMetodo = ingresoRepository.sumMontoByUsuarioIdGroupByMetodoPago(usuarioId);
        for (Object[] row : ingresosPorMetodo) {
            String metodo = row[0].toString();
            BigDecimal monto = (BigDecimal) row[1];
            if (resultado.containsKey(metodo)) {
                resultado.get(metodo)[0] = monto;
            }
        }

        List<Object[]> gastosPorMetodo = gastoMetodoPagoRepository.sumMontoByUsuarioIdGroupByMetodo(usuarioId);
        for (Object[] row : gastosPorMetodo) {
            String metodo = row[0].toString();
            BigDecimal monto = (BigDecimal) row[1];
            if (resultado.containsKey(metodo)) {
                resultado.get(metodo)[1] = monto;
            }
        }

        return resultado;
    }

    private BigDecimal obtenerValorOCero(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}
