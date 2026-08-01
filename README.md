# AgroSmart — Plataforma de Comercialización Agrícola

Proyecto desarrollado para el examen final práctico de **Programación Avanzada** de la **Universidad de las Fuerzas Armadas ESPE**.

AgroSmart es una aplicación backend que:

- Persiste productos agrícolas mediante **JPA/Hibernate**.
- Publica reactivamente los productos comercializables con **Spring WebFlux**.
- Genera textos publicitarios mediante **LangChain4j**.

---

## Autor

- **Nombre:** Juan Diego Albarracin Hidalgo
- **NRC:** 30405
- **Código del examen:** AGSK-2026

---

## Semilla personal

Los dos últimos dígitos de mi cédula son **34**.

| Parámetro | Valor |
|---|---|
| NN | `34` |
| Tabla | `tbl_productos_base_34` |
| Puerto | `8134` |
| Categoría | Banano |
| Audiencia de IA | supermercados mayoristas |
| Base de datos | `agrosmart_db` |

La tabla se obtiene agregando `34` al prefijo obligatorio:

```text
tbl_productos_base_
```

El puerto se obtiene agregando esos mismos dígitos a `81`:

```text
81 + 34 → 8134
```

---

## Tecnologías utilizadas

- Java 21
- Spring Boot 4.1.0
- Spring WebFlux
- Project Reactor
- Spring Data JPA
- Hibernate
- PostgreSQL
- Docker Compose
- LangChain4j
- JUnit 5
- Mockito
- Reactor Test
- StepVerifier
- Maven

---

## Arquitectura

La aplicación mantiene separadas la entidad de persistencia y el modelo de dominio:

| Componente | Responsabilidad |
|---|---|
| `ProductoEntity` | Entidad mutable utilizada por Hibernate |
| `Producto` | Modelo de dominio inmutable |
| `ProductoRepository` | Acceso bloqueante mediante JPA |
| `ProductoService` | Flujo reactivo de productos |
| `AgroSmartAIService` | Contrato declarativo de LangChain4j |
| `PublicidadService` | Adaptación reactiva de la llamada bloqueante a IA |
| `AgroSmartController` | Endpoints reactivos con WebFlux |

---

## Regla de negocio

Un producto es comercializable cuando cumple simultáneamente las siguientes condiciones:

```text
precioUsd > 0
```

y

```text
correosNotificacion no está vacía
```

La base contiene cinco productos de la categoría **Banano**:

- Tres productos válidos.
- Un producto inválido con precio igual a cero.
- Un producto inválido con correos vacíos.

---

## Requisitos de ejecución

Para ejecutar el proyecto se requiere:

- Java 21
- Docker Desktop
- Git
- Maven Wrapper incluido en el proyecto

---

## Ejecución

Primero debe iniciarse **Docker Desktop**.

Desde la raíz del proyecto, ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

Spring Boot utiliza el archivo `compose.yaml` para levantar PostgreSQL con la siguiente configuración:

| Parámetro | Valor |
|---|---|
| Base de datos | `agrosmart_db` |
| Usuario | `agrosmart` |
| Puerto del contenedor | `5432` |

La aplicación queda disponible en:

```text
http://localhost:8134
```

El perfil activo es `prod`, configurado mediante:

```properties
spring.profiles.active=prod
```

---

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/productos` | Devuelve los productos comercializables |
| `GET` | `/api/productos/{id}` | Busca un producto por identificador |
| `GET` | `/api/agrosmart/publicidad` | Genera una frase publicitaria |

---

## Consultar productos comercializables

```powershell
curl.exe http://localhost:8134/api/productos
```

El flujo devuelve los tres productos que cumplen la regla de negocio.

---

## Consultar un producto por ID

```powershell
curl.exe http://localhost:8134/api/productos/1
```

### Consultar un ID inexistente

```powershell
curl.exe -i http://localhost:8134/api/productos/9999
```

Resultado esperado:

```http
HTTP/1.1 404 Not Found
```

`ProductoNoEncontradoException` está anotada con:

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
```

---

## Generar publicidad

```powershell
curl.exe "http://localhost:8134/api/agrosmart/publicidad?producto=Banano%20organico%20Cavendish&audiencia=supermercados%20mayoristas"
```

La respuesta se devuelve como texto plano.

Si el proveedor de IA no está disponible, el operador `onErrorResume` devuelve un mensaje de respaldo sin propagar el error al endpoint.

---

## Operadores reactivos utilizados

### `Mono.fromCallable`

Envuelve las operaciones bloqueantes y difiere su ejecución hasta que exista una suscripción.

Se utiliza para:

- `repository.findAll()`
- `repository.findById()`
- La llamada a LangChain4j

### `subscribeOn(Schedulers.boundedElastic())`

Traslada las operaciones bloqueantes de JPA y de la llamada HTTP de IA a un conjunto de hilos preparado para tareas que pueden esperar.

De esta manera se evita bloquear el **event loop de Netty**.

### `flatMapMany`

Convierte el `Mono<List<ProductoEntity>>` producido por `findAll()` en un `Flux<ProductoEntity>` que emite cada entidad individualmente.

### `map`

Se utiliza para:

- Convertir `ProductoEntity` a `Producto`.
- Crear un producto nuevo con su nombre en mayúsculas.

### `filter`

Aplica el predicado:

```java
ProductoFilters.IS_VALID
```

Este operador conserva únicamente los productos que tienen:

- Precio positivo.
- Correos de notificación.

### `doOnNext`

Ejecuta:

```java
ProductoFilters.LOG_PRODUCTO
```

Se utiliza como efecto de trazabilidad sin transformar el producto emitido.

### `defaultIfEmpty`

En `obtenerProductosComercializables()` emite un producto genérico cuando todos los registros fueron descartados por el filtro.

### `switchIfEmpty`

En `buscarPorId()` cambia un `Mono` vacío por un `Mono.error` que contiene una `ProductoNoEncontradoException`.

### `timeout`

Limita a 30 segundos el tiempo máximo de respuesta del proveedor de IA.

### `onErrorResume`

Recupera el flujo cuando falla la IA y devuelve un mensaje de respaldo.

---

## Puente bloqueante a reactivo

JPA/Hibernate utiliza JDBC y realiza operaciones bloqueantes.

LangChain4j también realiza una llamada HTTP cuyo método espera la respuesta del proveedor.

Por esta razón, las operaciones se implementaron mediante:

```java
Mono.fromCallable(...)
        .subscribeOn(Schedulers.boundedElastic());
```

`fromCallable` evita que la operación se ejecute antes de la suscripción.

`boundedElastic` impide que la espera ocurra en un hilo `reactor-http-nio` perteneciente al event loop de Netty.

---

## Pruebas

Las pruebas son unitarias y no dependen de PostgreSQL ni de una conexión a internet.

El repositorio y el servicio de IA se simulan mediante **Mockito**.

Para ejecutar las pruebas:

```powershell
.\mvnw.cmd test
```

Se verifican los siguientes escenarios:

- Getters y copias defensivas de `Producto`.
- Producto válido.
- Producto inválido con precio cero.
- Producto inválido con correos vacíos.
- Creación de una instancia nueva mediante `A_MAYUSCULAS`.
- Emisión de tres productos comercializables.
- Emisión de un producto genérico cuando todos son inválidos.
- Error cuando el identificador no existe.
- Camino exitoso de la IA.
- Mensaje de respaldo cuando falla la IA.

---

## Evidencias

Las capturas de ejecución se encuentran en:

```text
docs/evidencias/
```

Las evidencias incluyen:

- Arranque de la aplicación.
- Esquema de PostgreSQL.
- Registros sembrados.
- Endpoints ejecutados con `curl`.
- Pruebas en verde.
- Historial de Git.

---

## Ejecución rápida

Clonar el repositorio:

```bash
git clone https://github.com/Jota2105/agrosmart-examen-final.git
```

Ingresar al directorio del proyecto:

```bash
cd agrosmart-examen-final
```

Ejecutar la aplicación:

```powershell
.\mvnw.cmd spring-boot:run
```

En otra terminal, consultar los productos comercializables:

```powershell
curl.exe http://localhost:8134/api/productos
```