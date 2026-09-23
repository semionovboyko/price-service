package com.bcnc.priceservice.infrastructure.adapter.in.web.mapper;

import com.bcnc.priceservice.domain.model.Price;
import com.bcnc.priceservice.infrastructure.adapter.in.web.dto.PriceResponse;
import org.springframework.stereotype.Component;

/**
 * Traduce {@link Price} (dominio) a {@link PriceResponse} (contrato REST). Es el equivalente,
 * en el lado de entrada, del
 * {@link com.bcnc.priceservice.infrastructure.adapter.out.persistence.mapper.PriceEntityMapper}
 * del lado de salida: cada adaptador tiene su propio mapper porque el punto donde el modelo de
 * dominio "cruza la frontera" hacia cada tecnología concreta es distinto.
 */
@Component
public class PriceWebMapper {

    /**
     * @param price tarifa resuelta por el caso de uso
     * @return el DTO de respuesta, con {@link java.util.Currency} convertida a su código ISO ({@code String})
     */
    public PriceResponse toResponse(Price price) {
        return new PriceResponse(
                price.getProductId(),
                price.getBrandId(),
                price.getPriceList(),
                price.getStartDate(),
                price.getEndDate(),
                price.getAmount(),
                price.getCurrency().getCurrencyCode());
    }
}
