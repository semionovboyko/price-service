package com.bcnc.priceservice.application.service;

import com.bcnc.priceservice.domain.exception.PriceNotFoundException;
import com.bcnc.priceservice.domain.model.Price;
import com.bcnc.priceservice.domain.port.in.FindApplicablePriceQuery;
import com.bcnc.priceservice.domain.port.in.FindApplicablePriceUseCase;
import com.bcnc.priceservice.domain.port.out.PriceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementación del caso de uso {@link FindApplicablePriceUseCase}. Deliberadamente delgada:
 * orquesta la llamada al puerto de salida y traduce la ausencia de resultado a una excepción de
 * dominio, sin decidir ella misma qué tarifa gana por prioridad — esa lógica vive en la consulta
 * del adaptador de persistencia (ver {@link PriceRepositoryPort}). Responsabilidad única (SOLID-S):
 * esta clase solo tiene una razón para cambiar, que es cómo se orquesta el caso de uso.
 *
 * <p>Es la única clase de {@code application} que conoce Spring (vía {@code @Service}, para que
 * el contenedor la gestione y la inyecte donde se pida {@link FindApplicablePriceUseCase}), pero
 * no conoce JPA ni HTTP: solo habla con {@link PriceRepositoryPort}, una abstracción del dominio.
 */
@Service
@RequiredArgsConstructor
public class PriceService implements FindApplicablePriceUseCase {

    private final PriceRepositoryPort priceRepositoryPort;

    /**
     * {@inheritDoc}
     *
     * @throws PriceNotFoundException si {@link PriceRepositoryPort} no devuelve ninguna tarifa aplicable
     */
    @Override
    public Price findApplicablePrice(FindApplicablePriceQuery query) {
        return priceRepositoryPort
                .findApplicablePrice(query.brandId(), query.productId(), query.applicationDate())
                .orElseThrow(() -> new PriceNotFoundException(
                        query.brandId(), query.productId(), query.applicationDate()));
    }
}
