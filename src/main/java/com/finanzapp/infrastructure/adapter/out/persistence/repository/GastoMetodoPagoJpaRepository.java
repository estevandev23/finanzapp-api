package com.finanzapp.infrastructure.adapter.out.persistence.repository;

import com.finanzapp.infrastructure.adapter.out.persistence.entity.GastoMetodoPagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GastoMetodoPagoJpaRepository extends JpaRepository<GastoMetodoPagoEntity, UUID> {

    @Query(value = """
        SELECT gmp.metodo, COALESCE(SUM(gmp.monto), 0)
        FROM gasto_metodo_pago gmp
        INNER JOIN gastos g ON g.id = gmp.gasto_id
        WHERE g.usuario_id = :usuarioId
        GROUP BY gmp.metodo
    """, nativeQuery = true)
    List<Object[]> sumMontoByUsuarioIdGroupByMetodo(@Param("usuarioId") UUID usuarioId);
}
