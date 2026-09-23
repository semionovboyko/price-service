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

El proyecto sigue **arquitectura hexagonal (puertos y adaptadores)**, con el dominio completamente libre de dependencias de framework:

```
src/main/java/com/bcnc/priceservice/
├── PriceServiceApplication.java
├── domain/                              → núcleo de negocio, sin Spring ni JPA
│   ├── model/Price.java                 → value object inmutable
│   ├── port/in/                         → puertos de entrada (casos de uso)
│   ├── port/out/                        → puertos de salida (persistencia)
│   └── exception/PriceNotFoundException.java
├── application/service/PriceService.java → orquesta el caso de uso
└── infrastructure/
    ├── adapter/in/web/                  → adaptador REST (driving adapter)
    │   ├── PriceController.java
    │   ├── dto/, mapper/, exception/
    └── adapter/out/persistence/         → adaptador JPA/H2 (driven adapter)
        ├── PriceEntity.java, PriceJpaRepository.java, PriceRepositoryAdapter.java
        └── mapper/
```

- **`domain`** no importa nada de Spring ni de JPA: se compila y se testea de forma completamente aislada.
- **`application`** orquesta el caso de uso (`PriceService`), dependiendo solo de interfaces del dominio (inversión de dependencias, SOLID-D).
- **`infrastructure`** contiene los adaptadores concretos: el adaptador web (entrada) y el adaptador de persistencia JPA/H2 (salida). Cada uno tiene su propio DTO/entidad y su propio mapper hacia/desde el modelo de dominio, para que un cambio en la tecnología de una capa no obligue a tocar el dominio.

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
