package com.bcnc.priceservice.domain.model;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

/**
 * Tarifa de precio para un producto de una cadena, válida durante un rango de fechas.
 *
 * <p>Es un <b>value object</b> en el sentido de Domain-Driven Design: no tiene una identidad
 * propia relevante para el dominio (dos instancias con los mismos valores son intercambiables),
 * por eso es inmutable y se compara por sus campos ({@code @Value} genera {@code equals} y
 * {@code hashCode} a partir de todos ellos). No conoce JPA ni Spring — solo depende de tipos del
 * propio JDK ({@link java.math.BigDecimal}, {@link java.time.LocalDateTime},
 * {@link java.util.Currency}), lo que permite que el módulo {@code domain} se compile y se
 * testee sin ningún framework en el classpath.
 */
@Value
@Builder
public class Price {

    Long brandId;
    LocalDateTime startDate;
    LocalDateTime endDate;
    Integer priceList;
    Long productId;
    Integer priority;
    BigDecimal amount;
    Currency currency;


    /**
     * Indica si esta tarifa cubre la fecha/hora dada. El rango es inclusivo en ambos extremos,
     * a propósito: coincide con el operador {@code BETWEEN} de la consulta JPQL del adaptador de
     * persistencia ({@link com.bcnc.priceservice.infrastructure.adapter.out.persistence.PriceJpaRepository}),
     * así que este método replica exactamente el mismo criterio en memoria.
     *
     * <p>Intencionadamente, no se invoca desde el flujo de producción: por eficiencia de
     * extracción de datos, el filtrado por rango se delega en la propia consulta SQL en vez de
     * traer candidatos a memoria para filtrarlos aquí. Se mantiene como parte del dominio porque
     * expresa esta regla de negocio de forma explícita e independiente de la capa de persistencia,
     * y está cubierto por un test unitario propio
     * ({@code com.bcnc.priceservice.domain.model.PriceTest}) que lo ejercita de forma aislada.
     *
     * @param applicationDate fecha/hora a comprobar
     * @return {@code true} si {@code applicationDate} está entre {@code startDate} y {@code endDate}, ambos incluidos
     */
    public boolean appliesAt(LocalDateTime applicationDate) {
        return !applicationDate.isBefore(startDate) && !applicationDate.isAfter(endDate);
    }
}
