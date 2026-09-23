package com.bcnc.priceservice.domain.port.in;

import com.bcnc.priceservice.domain.model.Price;

/**
 * Puerto de entrada (<i>driving port</i>, en la terminología de arquitectura hexagonal /
 * puertos y adaptadores): el contrato que la aplicación ofrece al exterior, sin comprometerse
 * a ninguna tecnología concreta de entrada (HTTP, mensajería, CLI...).
 *
 * <p>Lo implementa {@link com.bcnc.priceservice.application.service.PriceService}; lo invoca
 * {@link com.bcnc.priceservice.infrastructure.adapter.in.web.PriceController}, que depende de
 * esta interfaz y no de la implementación concreta — inversión de dependencias (principio D de
 * SOLID). Gracias a ello, el controller se puede testear con un mock de esta interfaz sin
 * levantar base de datos ni el resto del contexto de Spring.
 */
public interface FindApplicablePriceUseCase {

    /**
     * Resuelve la tarifa de mayor prioridad aplicable a los parámetros dados.
     *
     * @param query producto, cadena y fecha de aplicación, ya validados
     * @return la tarifa aplicable
     * @throws com.bcnc.priceservice.domain.exception.PriceNotFoundException si no existe ninguna tarifa que cubra esa fecha
     */
    Price findApplicablePrice(FindApplicablePriceQuery query);
}
