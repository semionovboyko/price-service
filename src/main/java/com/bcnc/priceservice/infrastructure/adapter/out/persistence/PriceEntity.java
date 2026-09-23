package com.bcnc.priceservice.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA que mapea la tabla {@code PRICES}. Vive exclusivamente en {@code infrastructure}:
 * el dominio nunca ve esta clase, solo {@link PriceRepositoryAdapter} la traduce a
 * {@link com.bcnc.priceservice.domain.model.Price} a través de
 * {@link com.bcnc.priceservice.infrastructure.adapter.out.persistence.mapper.PriceEntityMapper}.
 *
 * <p>Se añade un {@code id} técnico ({@link GenerationType#IDENTITY}) como clave primaria porque
 * la tabla del enunciado no define ninguna explícita; el propio enunciado permite añadir campos
 * nuevos. {@code IDENTITY} delega en el autoincremental nativo de H2 — es la estrategia más simple
 * y suficiente para una base en memoria de un único nodo; en un sistema distribuido con múltiples
 * instancias escribiendo a la vez se preferiría {@code SEQUENCE} con un tamaño de bloque
 * ({@code allocationSize}), para no depender de una vuelta a la base de datos por cada inserción.
 *
 * <p>Deliberadamente, <b>no</b> se usa {@code @Data} de Lombok aquí: genera {@code equals}/
 * {@code hashCode} basados en todos los campos, lo cual es un problema conocido en entidades JPA
 * (con colecciones lazy o proxies de Hibernate de por medio, puede romper el contrato de
 * {@code equals}). Con {@code @Getter}/{@code @Setter} es suficiente para lo que necesita esta
 * entidad — no participa en ninguna colección ni relación bidireccional.
 */
@Entity
@Table(name = "PRICES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceEntity {

    /** Clave primaria técnica; ver la nota de la clase sobre {@link GenerationType#IDENTITY}. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "BRAND_ID", nullable = false)
    private Long brandId;

    @Column(name = "START_DATE", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "END_DATE", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "PRICE_LIST", nullable = false)
    private Integer priceList;

    @Column(name = "PRODUCT_ID", nullable = false)
    private Long productId;

    @Column(name = "PRIORITY", nullable = false)
    private Integer priority;

    @Column(name = "PRICE", nullable = false)
    private BigDecimal price;

    @Column(name = "CURR", nullable = false, length = 3)
    private String curr;
}
