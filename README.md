# price-service

Prueba técnica — servicio Spring Boot que expone un endpoint REST para resolver la tarifa de precio aplicable a un producto de una cadena en una fecha concreta, a partir de un conjunto de tarifas con rangos de vigencia y prioridad.

## Stack técnico

- **Java 21**
- **Spring Boot 4.1.1** (Spring Framework 7.0.9, Hibernate ORM 7.4.5)
- **Maven** (con Maven Wrapper incluido)
- **Spring Data JPA** + **H2** (base de datos en memoria)
- **Lombok**
- **JUnit 5 + Mockito + AssertJ + MockMvc** (testing)

## Cómo ejecutar

No hace falta instalar Maven: el proyecto incluye el Maven Wrapper.

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

La aplicación arranca en `http://localhost:8080`. Al arrancar, Hibernate crea el esquema y `data.sql` siembra automáticamente 4 tarifas de ejemplo en la base de datos H2 en memoria — no requiere ningún paso manual de configuración de datos.

Consola de H2 disponible en `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:pricedb;DB_CLOSE_DELAY=-1`, usuario `sa`, sin contraseña).

## API

### `GET /api/v1/prices`

Devuelve la tarifa de precio aplicable a un producto de una cadena en una fecha dada. Cuando varias tarifas coinciden en el rango, se devuelve la de mayor prioridad (`PRIORITY`) — el endpoint siempre devuelve un único resultado.

**Parámetros (query params, todos obligatorios):**

| Parámetro | Tipo | Formato | Descripción |
|---|---|---|---|
| `brandId` | Long | — | Identificador de la cadena (p. ej. `1` = ZARA) |
| `productId` | Long | — | Identificador del producto |
| `applicationDate` | LocalDateTime | ISO-8601 (`yyyy-MM-dd'T'HH:mm:ss`) | Fecha/hora de aplicación |

**Ejemplo de petición:**

```bash
curl "http://localhost:8080/api/v1/prices?brandId=1&productId=35455&applicationDate=2020-06-14T16:00:00"
```

**Respuesta `200 OK`:**

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 2,
  "startDate": "2020-06-14T15:00:00",
  "endDate": "2020-06-14T18:30:00",
  "price": 25.45,
  "currency": "EUR"
}
```

**Respuesta `404 Not Found`** (no existe ninguna tarifa aplicable a esa fecha):

```json
{
  "status": 404,
  "message": "No se encontró ninguna tarifa aplicable para brandId=1, productId=35455, fecha=2019-01-01T00:00",
  "timestamp": "2026-09-23T16:00:00"
}
```

**Respuesta `400 Bad Request`** (parámetro obligatorio ausente, o con formato inválido):

```json
{
  "status": 400,
  "message": "Parámetros de entrada inválidos: ...",
  "timestamp": "2026-09-23T16:00:00"
}
```

### Casos de prueba del enunciado

Con `brandId=1` y `productId=35455`, las 4 tarifas de ejemplo (`data.sql`) producen estos 5 resultados:

| # | `applicationDate` | `priceList` | Precio |
|---|---|---|---|
| 1 | 2020-06-14T10:00:00 | 1 | 35.50 € |
| 2 | 2020-06-14T16:00:00 | 2 | 25.45 € |
| 3 | 2020-06-14T21:00:00 | 1 | 35.50 € |
| 4 | 2020-06-15T10:00:00 | 3 | 30.50 € |
| 5 | 2020-06-16T21:00:00 | 4 | 38.95 € |

Los casos 2 y 4 son los que validan el desempate por prioridad, ya que en esos instantes hay más de una tarifa vigente.

## Arquitectura

El proyecto sigue **arquitectura hexagonal (puertos y adaptadores)**, con el dominio completamente libre de dependencias de framework.

El paquete `domain` contiene el núcleo de negocio: el modelo (`Price`, como value object inmutable), los puertos de entrada (`port/in`, que definen los casos de uso) y los puertos de salida (`port/out`, que definen el contrato de persistencia sin comprometerse a una tecnología concreta), además de las excepciones propias del dominio como `PriceNotFoundException`. Esta capa no importa nada de Spring ni de JPA: se compila y se testea de forma completamente aislada, sin necesidad de levantar contexto ni base de datos.

El paquete `application` contiene `PriceService`, que orquesta el caso de uso implementando el puerto de entrada correspondiente. Depende únicamente de interfaces del dominio, nunca de implementaciones concretas — es la inversión de dependencias de SOLID (el principio D) aplicada de forma directa: el dominio define el contrato, la infraestructura lo implementa, nunca al revés.

El paquete `infrastructure` contiene los adaptadores concretos, divididos en dos direcciones. El adaptador de entrada (`adapter/in/web`) expone el caso de uso vía REST: `PriceController`, sus DTOs, su mapper hacia/desde el dominio y el manejo centralizado de errores. El adaptador de salida (`adapter/out/persistence`) implementa el puerto de persistencia contra JPA/H2: la entidad `PriceEntity`, el repositorio Spring Data (`PriceJpaRepository`) y el adaptador (`PriceRepositoryAdapter`) que traduce entre el mundo JPA y el modelo de dominio. Cada adaptador tiene su propio mapper, de forma que un cambio en la tecnología de una capa —por ejemplo, sustituir H2/JPA por MongoDB, o REST por un consumer de Kafka— no obliga a tocar el dominio ni el caso de uso.

### Decisiones de diseño relevantes

- **Eficiencia de extracción de datos:** el filtrado por rango de fechas y el desempate por prioridad se resuelven en la propia consulta SQL (`WHERE ... BETWEEN ... ORDER BY priority DESC`) junto con `Pageable`/`PageRequest.of(0, 1)`, de forma que la base de datos devuelve directamente un único resultado — no se traen candidatos a memoria para filtrarlos en Java.
- **Manejo de errores centralizado** en `GlobalExceptionHandler` (`@RestControllerAdvice`): 404 cuando no hay tarifa aplicable, 400 cuando los parámetros de entrada son inválidos.
- **DTOs propios** (`PriceResponse`, `ErrorResponse`) desacoplados del modelo de dominio: un cambio en el contrato de la API no obliga a tocar `Price`, y viceversa.

## Testing

```bash
./mvnw test
```

La suite incluye:

- **`PriceServiceApplicationTests`** — smoke test de que el contexto de Spring levanta correctamente.
- **`PriceTest`** — test unitario puro (sin Spring) de la regla de negocio `Price.appliesAt`.
- **`PriceServiceTest`** — test unitario de `PriceService` con Mockito, mockeando el puerto de persistencia.
- **`PriceControllerIntegrationTest`** — test de integración end-to-end (`@SpringBootTest` + MockMvc) contra la base de datos H2 real sembrada por `data.sql`: cubre los 5 casos obligatorios del enunciado más los casos de error (404 y 400).
