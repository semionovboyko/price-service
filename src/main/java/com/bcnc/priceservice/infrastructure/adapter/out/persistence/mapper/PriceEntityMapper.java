package com.bcnc.priceservice.infrastructure.adapter.out.persistence.mapper;

import com.bcnc.priceservice.domain.model.Price;
import com.bcnc.priceservice.infrastructure.adapter.out.persistence.PriceEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

/**
 * Traduce entre el modelo de persistencia ({@link PriceEntity}, con anotaciones JPA) y el
 * modelo de dominio ({@link Price}, sin ninguna). Es un mapper manual y a propósito: para un
 * proyecto de este tamaño no compensa añadir MapStruct como dependencia extra, y así queda
 * explícito, en una sola clase de pocas líneas, el punto exacto donde "cruza la frontera" del
 * hexágono en el lado de salida.
 */
@Component
public class PriceEntityMapper {

    /**
     * @param entity fila leída de la tabla {@code PRICES}
     * @return el equivalente en el modelo de dominio, con {@code curr} (String ISO) convertido a {@link Currency}
     */
    public Price toDomain(PriceEntity entity) {
        return Price.builder()
                .brandId(entity.getBrandId())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .priceList(entity.getPriceList())
                .productId(entity.getProductId())
                .priority(entity.getPriority())
                .amount(entity.getPrice())
                .currency(Currency.getInstance(entity.getCurr()))
                .build();
    }
}
