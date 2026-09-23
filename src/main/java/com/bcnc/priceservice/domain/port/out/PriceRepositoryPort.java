package com.bcnc.priceservice.domain.port.out;

import com.bcnc.priceservice.domain.model.Price;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Puerto de salida (<i>driven port</i>): lo que el dominio necesita del exterior (persistencia),
 * expresado como una abstracción que no menciona JPA, SQL ni ninguna tecnología concreta.
 * Lo implementa {@link com.bcnc.priceservice.infrastructure.adapter.out.persistence.PriceRepositoryAdapter},
 * apoyado en Spring Data JPA — pero el dominio no lo sabe ni le importa.
 *
 * <p><b>Importante:</b> la resolución de "qué tarifa gana" cuando varias solapan en fechas
 * (mayor {@code PRIORITY}) se delega por completo a la consulta del adaptador de persistencia
 * ({@code ORDER BY priority DESC} + {@code LIMIT} vía {@link org.springframework.data.domain.Pageable}),
 * no se trae la lista completa de candidatas a memoria para filtrar aquí. Es precisamente lo
 * que el enunciado evalúa como "eficiencia de la extracción de datos": la base de datos ya
 * devuelve como mucho una fila.
 */
public interface PriceRepositoryPort {

    /**
     * Busca la tarifa de mayor prioridad que cubre la fecha de aplicación dada.
     *
     * @param brandId identificador de la cadena
     * @param productId identificador del producto
     * @param applicationDate fecha/hora para la que se busca tarifa aplicable
     * @return la tarifa aplicable, o {@link Optional#empty()} si ninguna cubre esa fecha
     */
    Optional<Price> findApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate);
}
