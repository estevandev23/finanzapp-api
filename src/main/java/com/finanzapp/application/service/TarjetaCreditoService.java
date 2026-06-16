package com.finanzapp.application.service;

import com.finanzapp.domain.exception.AccesoDenegadoException;
import com.finanzapp.domain.exception.DomainException;
import com.finanzapp.domain.exception.RecursoNotFoundException;
import com.finanzapp.domain.model.EstadoTarjeta;
import com.finanzapp.domain.model.TarjetaCredito;
import com.finanzapp.domain.port.in.TarjetaCreditoUseCase;
import com.finanzapp.domain.port.out.TarjetaCreditoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TarjetaCreditoService implements TarjetaCreditoUseCase {

    private final TarjetaCreditoRepositoryPort tarjetaRepository;

    @Override
    public TarjetaCredito crear(TarjetaCredito tarjeta) {
        validarNueva(tarjeta);

        tarjeta.setId(UUID.randomUUID());
        LocalDateTime ahora = LocalDateTime.now();
        tarjeta.setFechaCreacion(ahora);
        tarjeta.setFechaActualizacion(ahora);
        if (tarjeta.getCupoUsado() == null) {
            tarjeta.setCupoUsado(BigDecimal.ZERO);
        }
        if (tarjeta.getEstado() == null) {
            tarjeta.setEstado(EstadoTarjeta.ACTIVA);
        }
        return tarjetaRepository.save(tarjeta);
    }

    @Override
    @Transactional(readOnly = true)
    public TarjetaCredito obtenerPorId(UUID id) {
        return tarjetaRepository.findById(id)
                .orElseThrow(() -> new RecursoNotFoundException("TarjetaCredito", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TarjetaCredito> listarPorUsuario(UUID usuarioId) {
        return tarjetaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public TarjetaCredito actualizar(UUID id, TarjetaCredito datos) {
        TarjetaCredito actual = obtenerPorId(id);
        if (datos.getNombre() != null) actual.setNombre(datos.getNombre());
        if (datos.getBanco() != null) actual.setBanco(datos.getBanco());
        if (datos.getUltimosCuatro() != null) actual.setUltimosCuatro(datos.getUltimosCuatro());
        if (datos.getCupoTotal() != null) {
            if (datos.getCupoTotal().signum() <= 0) {
                throw new DomainException("cupoTotal debe ser mayor a 0");
            }
            actual.setCupoTotal(datos.getCupoTotal());
        }
        if (datos.getCupoUsado() != null) {
            if (datos.getCupoUsado().signum() < 0) {
                throw new DomainException("El saldo usado no puede ser negativo");
            }
            actual.setCupoUsado(datos.getCupoUsado());
        }
        if (actual.getCupoUsado() != null
                && actual.getCupoTotal().compareTo(actual.getCupoUsado()) < 0) {
            throw new DomainException("El cupo total no puede ser menor al saldo usado");
        }
        if (datos.getDiaCorte() > 0) actual.setDiaCorte(datos.getDiaCorte());
        if (datos.getDiaPago() > 0) actual.setDiaPago(datos.getDiaPago());
        if (datos.getColor() != null) actual.setColor(datos.getColor());
        if (datos.getEstado() != null) actual.setEstado(datos.getEstado());
        actual.setFechaActualizacion(LocalDateTime.now());
        return tarjetaRepository.save(actual);
    }

    @Override
    public void eliminar(UUID id) {
        TarjetaCredito tarjeta = obtenerPorId(id);
        if (tarjeta.getCupoUsado() != null && tarjeta.getCupoUsado().signum() > 0) {
            throw new DomainException("No se puede eliminar una tarjeta con cupo en uso. " +
                    "Bloquéala o cancélala en su lugar.");
        }
        tarjetaRepository.deleteById(id);
    }

    @Override
    public void aumentarCupoUsado(UUID tarjetaId, BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) return;
        TarjetaCredito tarjeta = obtenerPorId(tarjetaId);
        if (tarjeta.getEstado() != EstadoTarjeta.ACTIVA) {
            throw new DomainException("La tarjeta no está activa");
        }
        BigDecimal usadoActual = tarjeta.getCupoUsado() != null ? tarjeta.getCupoUsado() : BigDecimal.ZERO;
        BigDecimal nuevoUsado = usadoActual.add(monto);
        if (nuevoUsado.compareTo(tarjeta.getCupoTotal()) > 0) {
            throw new DomainException("El gasto excede el cupo disponible de la tarjeta");
        }
        tarjeta.setCupoUsado(nuevoUsado);
        tarjeta.setFechaActualizacion(LocalDateTime.now());
        tarjetaRepository.save(tarjeta);
    }

    @Override
    public void disminuirCupoUsado(UUID tarjetaId, BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) return;
        TarjetaCredito tarjeta = obtenerPorId(tarjetaId);
        BigDecimal usadoActual = tarjeta.getCupoUsado() != null ? tarjeta.getCupoUsado() : BigDecimal.ZERO;
        tarjeta.setCupoUsado(usadoActual.subtract(monto).max(BigDecimal.ZERO));
        tarjeta.setFechaActualizacion(LocalDateTime.now());
        tarjetaRepository.save(tarjeta);
    }

    public TarjetaCredito obtenerPorIdValidado(UUID id, UUID usuarioId) {
        TarjetaCredito tarjeta = obtenerPorId(id);
        if (!tarjeta.getUsuarioId().equals(usuarioId)) {
            throw new AccesoDenegadoException("tarjeta de crédito");
        }
        return tarjeta;
    }

    public TarjetaCredito actualizarValidado(UUID id, TarjetaCredito datos, UUID usuarioId) {
        obtenerPorIdValidado(id, usuarioId);
        return actualizar(id, datos);
    }

    public void eliminarValidado(UUID id, UUID usuarioId) {
        obtenerPorIdValidado(id, usuarioId);
        eliminar(id);
    }

    private void validarNueva(TarjetaCredito tarjeta) {
        if (tarjeta.getUsuarioId() == null) throw new DomainException("usuarioId es requerido");
        if (tarjeta.getNombre() == null || tarjeta.getNombre().isBlank()) {
            throw new DomainException("nombre es requerido");
        }
        if (tarjeta.getCupoTotal() == null || tarjeta.getCupoTotal().signum() <= 0) {
            throw new DomainException("cupoTotal debe ser mayor a 0");
        }
        if (tarjeta.getDiaCorte() < 1 || tarjeta.getDiaCorte() > 31) {
            throw new DomainException("diaCorte debe estar entre 1 y 31");
        }
        if (tarjeta.getDiaPago() < 1 || tarjeta.getDiaPago() > 31) {
            throw new DomainException("diaPago debe estar entre 1 y 31");
        }
        if (tarjeta.getUltimosCuatro() != null && !tarjeta.getUltimosCuatro().matches("\\d{4}")) {
            throw new DomainException("ultimosCuatro debe contener exactamente 4 dígitos");
        }
    }
}
