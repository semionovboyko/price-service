package com.bcnc.priceservice.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio Spring Data JPA para {@link PriceEntity}. Al extender {@link JpaRepository} ya
 * hereda operaciones CRUD estándar ({@code findById}, {@code save}, {@code deleteAll}...); la
 * única consulta propia es la que resuelve la tarifa aplicable, ver más abajo.
 */
public interface PriceJpaRepository extends JpaRepository<PriceEntity, Long> {

    /**
     * Devuelve las tarifas que cubren {@code applicationDate}, ordenadas por prioridad
     * descendente. El criterio {@code :applicationDate BETWEEN p.startDate AND p.endDate} es el
     * equivalente JPQL de {@link com.bcnc.priceservice.domain.model.Price#appliesAt}.
     *
     * <p>La "eficiencia de extracción de datos" del criterio de evaluación se consigue con el
     * parámetro {@link Pageable}: al pedir una página de tamaño 1
     * ({@link org.springframework.data.domain.PageRequest#of(int, int)} en el adaptador), Spring
     * Data traduce eso a un {@code LIMIT} (o equivalente del dialecto) en el SQL real que genera
     * Hibernate — la base de datos ya devuelve como mucho una fila, no se trae el conjunto
     * completo de tarifas candidatas para filtrar la de mayor prioridad en Java.
     *
     * @param brandId identificador de la cadena
     * @param productId identificador del producto
     * @param applicationDate fecha/hora de aplicación
     * @param pageable página solicitada; el adaptador siempre pide tamaño 1
     * @return como mucho una tarifa (la de mayor prioridad), envuelta en una lista por contrato de Spring Data
     */
    @Query("""
            SELECT p FROM PriceEntity p
            WHERE p.brandId = :brandId
              AND p.productId = :productId
              AND :applicationDate BETWEEN p.startDate AND p.endDate
            ORDER BY p.priority DESC
            """)
    List<PriceEntity> findApplicablePricesByPriorityDesc(
            @Param("brandId") Long brandId,
            @Param("productId") Long productId,
            @Param("applicationDate") LocalDateTime applicationDate,
            Pageable pageable);
}
