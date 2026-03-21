package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.infrastructure.adapter.out.persistence.entity.ColaMensajeWhatsappEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColaMensajeWhatsappJpaRepository extends JpaRepository<ColaMensajeWhatsappEntity, Long> {

    long countByNumeroTelefonoAndProcesadoFalseAndIdGreaterThan(String numeroTelefono, Long id);

    List<ColaMensajeWhatsappEntity> findByNumeroTelefonoAndProcesadoFalseOrderByRecibidoEnAsc(String numeroTelefono);

    @Modifying
    @Query("UPDATE ColaMensajeWhatsappEntity c SET c.procesado = true WHERE c.numeroTelefono = :telefono AND c.procesado = false")
    int marcarComoProcesados(@Param("telefono") String telefono);
}
