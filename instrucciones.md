# Instrucciones para ejecutar y probar el sistema — Banco XYZ (EFT Semana 9)

Proyecto académico de PBY2203 Desarrollo Backend III (Duoc UC). Migra a microservicios los procesos del sistema legacy del Banco XYZ: procesos batch con Spring Batch, tres BFF (Web, Móvil y Cajero), microservicios de negocio con Spring Cloud, OAuth2, Resilience4j y Kafka, todo orquestado con Docker Compose.

Este documento explica, paso a paso, cómo levantar el sistema completo y cómo comprobar que **cada componente** funciona. El despliegue en la nube se explica en `despliegue.md`.

## 1. Qué se va a ejecutar

| Componente | Contenedor | Puerto en el equipo | Función |
|---|---|---|---|
| MySQL 8 | `mysql-batch-db` | 3306 | Base `bancoxyz_batch`, compartida por batch, cuentas, pagos y clientes |
| Kafka 3.7 | `bancoxyz-kafka` | 29092 (externo) / 9092 (red interna) | Broker de eventos (tópico `pagos-topic`) |
| `eureka-server` | `eureka-server` | 8761 | Registro y descubrimiento de servicios |
| `config-server` | `config-server` | 8888 | Configuración centralizada (modo nativo, carpeta `config-repo`) |
| `ms-auth-server` | `ms-auth-server` | 9000 | Servidor OAuth2 que emite los tokens JWT |
| `ms-batch-data` | `ms-batch-data` | 8080 | Los 3 procesos batch (movimientos, estados anuales, intereses) |
| `ms-cuentas` | `ms-cuentas` | 8082 | Microservicio de cuentas |
| `ms-pagos` | `ms-pagos` | 8083 | Microservicio de pagos; publica eventos en Kafka |
| `ms-clientes` | `ms-clientes` | 8084 | Microservicio de clientes; consume eventos de Kafka |
| `ms-bff-web` | `ms-bff-web` | 8081 | BFF para navegadores |
| `ms-bff-movil` | `ms-bff-movil` | 8085 | BFF para la app móvil |
| `ms-bff-cajero` | `ms-bff-cajero` | 8086 | BFF para cajeros automáticos |

Todos los contenedores comparten la red `bancoxyz-net`. Los servicios se hablan por nombre de contenedor (por ejemplo `http://eureka-server:8761`), no por `localhost`.

## 2. Requisitos previos

- **Docker Desktop** (Windows/macOS) o Docker Engine con el plugin Compose v2 (Linux). Verifica con `docker --version` y `docker compose version`.
- **Git** para clonar el repositorio.
- **Postman** para probar los endpoints.
- Puertos libres en el equipo: 3306, 8080–8086, 8761, 8888, 9000 y 29092. Si tienes un MySQL local en el 3306, detenlo o cambia el puerto publicado en `docker-compose.yml`.


## 3. Obtener el código

```bash
git clone <https://github.com/Rai-Morroni/PBY2203-Backend-III-2026-EFT.git>
cd "Proyecto EFT Semana 9"
```

La carpeta raíz contiene `docker-compose.yml` y un directorio por módulo (`eureka-server`, `config-server`, `ms-auth-server`, `ms-batch-data`, `ms-cuentas`, `ms-pagos`, `ms-clientes`, `ms-bff-web`, `ms-bff-movil`, `ms-bff-cajero`). Todos los comandos siguientes se ejecutan desde esa carpeta.

## 4. Levantar el sistema completo

```bash
docker compose up -d --build
```

- La **primera vez** podría tardar porque cada imagen descarga dependencias Maven y compila (con `-DskipTests`). Las siguientes veces usa caché y tarda menos de 2 minutos.
- El orden de arranque lo controla Compose: primero MySQL, Kafka, Eureka y Config Server (con *healthcheck*), y después el resto de servicios.

Comprueba el estado:

```bash
docker compose ps
```

Deben aparecer los **12 contenedores** en estado `running` y, para `mysql-batch`, `eureka-server` y `config-server`, `healthy`. Si algún servicio Java quedó en `restarting`, espera un minuto: puede haber arrancado antes que MySQL (ver sección 9).

Para seguir los logs de un servicio: `docker compose logs -f ms-cuentas` (cambia el nombre según el servicio).

## 5. Credenciales y datos de prueba

| Qué | Valor |
|---|---|
| MySQL (usuario / clave / base) | `root` / `admin` / `bancoxyz_batch` |
| Cliente OAuth2 (client credentials) | `bff-client` / `bff-secret` |
| Scopes del cliente | `read write` |
| Usuario de prueba del servidor de autorización | `usuario_banco` / `password123` |
| Vigencia del token | 299 segundos (pasado ese tiempo hay que pedir otro) |

Son credenciales de **desarrollo académico**. No deben usarse fuera de este entorno.

Los datos de entrada del batch están en `ms-batch-data/src/main/resources/data/`: `movimientos_financieros_diarios.csv`, `estados_financieros_anuales.csv` e `intereses_trimestrales.csv` (1000 filas cada uno, con datos sucios a propósito).

## 6. Pruebas por componente

Las pruebas siguen el orden natural de dependencias. Cada una indica el resultado esperado.

### 6.1 Infraestructura: MySQL y Kafka

MySQL responde y tiene la base:

```bash
docker exec mysql-batch-db mysql -uroot -padmin -e "SHOW DATABASES;"
```

Esperado: aparece `bancoxyz_batch`.

Kafka está arriba (el tópico `pagos-topic` se crea al primer mensaje, así que puede no aparecer todavía):

```bash
docker exec bancoxyz-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

### 6.2 Config Server

```bash
http://localhost:8888/ms-bff-web/default
```

Esperado: un JSON con `name: "ms-bff-web"` y la lista de `propertySources` que viene de `config-repo/ms-bff-web.yml`.

### 6.3 Eureka

Abre `http://localhost:8761` en el navegador. En "Instances currently registered with Eureka" deben aparecer **7 servicios** en estado `UP`: `MS-AUTH-SERVER`, `MS-CUENTAS`, `MS-PAGOS`, `MS-CLIENTES`, `MS-BFF-WEB`, `MS-BFF-MOVIL` y `MS-BFF-CAJERO`. `ms-batch-data` no se registra por diseño: se invoca directamente.

Si algún servicio tarda en aparecer, espera 10 a 30 segundos y recarga.

### 6.4 Servidor de autorización (OAuth2) — obtener un token

Este token se usa en las pruebas de los BFF.

**Postman:** `POST http://localhost:9000/oauth2/token`, pestaña *Authorization* → Basic Auth con usuario `bff-client` y contraseña `bff-secret`; pestaña *Body* → `x-www-form-urlencoded` con `grant_type = client_credentials` y `scope = read write`.

Esperado: respuesta **200** con `token_type: Bearer`, `expires_in` cercano a 299 y `scope: "read write"`. Con credenciales incorrectas: **401**.

### 6.5 Procesos batch (`ms-batch-data`, puerto 8080)

El batch expone tres endpoints `POST` que lanzan cada job. No requieren token.

| Job | Endpoint | Tabla destino |
|---|---|---|
| Movimientos diarios | `POST http://localhost:8080/api/batch/movimientos` | `movimientos_diarios` |
| Estados de cuenta anuales | `POST http://localhost:8080/api/batch/estados-anuales` | `estados_anuales` |
| Intereses mensuales | `POST http://localhost:8080/api/batch/intereses` | `intereses_mensuales` |

Ejecuta los tres, uno a uno. Cada respuesta es un JSON con `estado`, `jobExecutionId`, `leidos`, `escritos`, `filtrados`, `rechazados`, `duracionMs` y `mensaje`.

**Resultado esperado de la primera ejecución** (sobre los CSV entregados):

| Job | `estado` | `leidos` | `escritos` | `rechazados` |
|---|---|---|---|---|
| movimientos | `COMPLETED` | 1000 | 470 | 530 |
| estados-anuales | `COMPLETED` | 1000 | 836 | 164 |
| intereses | `COMPLETED` | 1000 | 397 | 603 |

Los registros rechazados no se pierden: quedan en la tabla `batch_rechazos` con el archivo, la línea y el motivo (por ejemplo `TIPO_INVALIDO`, `MONTO_INVALIDO`, `FECHA_INVALIDA`, `EDAD_FUERA_DE_RANGO`, `NOMBRE_INVALIDO`).

**Prueba de idempotencia.** Repite el mismo `POST`. Esperado: **409 Conflict** con el mensaje "El mismo archivo y parámetros ya fueron procesados; use forzar=true para reejecutar." No se escribe nada. Para reejecutar a propósito:

```bash
POST "http://localhost:8080/api/batch/movimientos?forzar=true"
```

Esperado: el job corre de nuevo y **el número de filas en la tabla no cambia** (cada fila se identifica por archivo y número de línea, y se escribe con *upsert*).


**Reglas aplicadas** (resumen; el detalle está en `ms-batch-data/docs/reglas-batch.md`):

- Fechas: se aceptan `yyyy-MM-dd`, `yyyy/MM/dd`, `dd/MM/yyyy` y `dd-MM-yyyy`; fechas imposibles o futuras se rechazan.
- Movimientos: `credito` se guarda con signo positivo y `debito` con signo negativo; `invalido` y `desconocido` se rechazan.
- Estados anuales: `deposito` suma, `compra`, `retiro` y `pago` restan; monto vacío o cero se rechaza.
- Intereses: solo `ahorro`, `prestamo` e `hipoteca`; edad entre 18 y 120; el interés mensual se calcula como saldo × tasa anual / 12 (2 % ahorro, 5 % préstamo e hipoteca) y se guarda junto al saldo final.



### 6.6 Microservicio de Cuentas (puerto 8082)

Primero busca una cuenta que exista en los datos cargados por el batch:

```bash
docker exec mysql-batch-db mysql -uroot -padmin bancoxyz_batch -e "SELECT cuenta_id, COUNT(*) movimientos, SUM(monto) saldo FROM movimientos_diarios GROUP BY cuenta_id ORDER BY cuenta_id LIMIT 5;"
```

Luego consulta una de ellas (reemplaza `2` por el `cuenta_id` obtenido):

```bash
http://localhost:8082/api/v1/cuentas/validar/2
```

Esperado: **200** con un texto del tipo `Cuenta 2 validada. Movimientos históricos: N. Saldo actual calculado: $X`, donde `N` y `X` coinciden con la consulta SQL anterior. Esta prueba demuestra que el batch alimenta al microservicio de cuentas a través de la base compartida.

### 6.7 Pagos → Kafka → Clientes (eventos en tiempo real)

Envía un pago al microservicio de pagos:

```bash
POST http://localhost:8083/api/v1/pagos/procesar/100
```

Esperado: **200** con "Pago procesado rápidamente. Notificación asíncrona enviada a clientes."

Comprueba que `ms-clientes` recibió el evento por Kafka:

```bash
docker logs ms-clientes --tail 30
```

Esperado: un bloque con **"EVENTO KAFKA RECIBIDO EN MS-CLIENTES"** que incluye el texto del pago por `$100`. Al arrancar, el log también muestra que el grupo `clientes-group` quedó asignado a `pagos-topic-0`. Con esto queda demostrado el ciclo productor (Pagos) → broker → consumidor (Clientes).

### 6.8 BFF Móvil (puerto 8085)

Respuestas ligeras con los datos esenciales de una cuenta. Exige token.

Sin token (esperado **401**):

```bash
curl -i http://localhost:8085/api/v1/movil/resumen/2
```

Con token (PowerShell; en Bash usa `-H "Authorization: Bearer $TOKEN"`):

```powershell
curl.exe -i -H "Authorization: Bearer $token" http://localhost:8085/api/v1/movil/resumen/2
```

Esperado: **200** con un JSON ligero con los datos de la cuenta, obtenidos mediante Feign desde `ms-cuentas` (el nombre del servicio se resuelve en Eureka). Usa una cuenta existente (sección 6.6).

### 6.9 BFF Web (puerto 8081)

Datos para interfaces complejas. Exige token. Tiene tres endpoints:

```powershell
curl.exe -i -H "Authorization: Bearer $token" http://localhost:8081/api/v1/web/reportes/movimientos-diarios
curl.exe -i -H "Authorization: Bearer $token" http://localhost:8081/api/v1/web/reportes/estados-anuales
curl.exe -i -H "Authorization: Bearer $token" http://localhost:8081/api/v1/web/reportes/intereses-mensuales
```

Esperado sin token: **401**. Con token válido: la petición se autentica y el BFF responde. Revisa la sección 10: en la versión actual la capa de datos del BFF Web aún no consulta información real y responde **204 No Content**.

### 6.10 BFF Cajero (puerto 8086)

Operaciones críticas de cajero automático. Exige token.

```powershell
curl.exe -i -X POST -H "Authorization: Bearer $token" "http://localhost:8086/api/v1/cajero/retiro?idCuenta=123&monto=50000"
```

Esperado: **200** con un JSON de confirmación. El BFF valida un límite máximo de 200000 por operación (un monto mayor es rechazado) y exige token (sin token, **401**). Revisa la sección 10: la respuesta de esta versión es simulada.

### 6.11 Resiliencia (Resilience4j / circuit breaker)

El BFF Móvil protege la llamada a `ms-cuentas` con un circuit breaker (`cuentasCB`: ventana de 10 llamadas, mínimo 5, umbral de fallos 50 %, espera de 10 s en estado abierto) y un método alternativo que responde **503** con un mensaje amigable.

1. Detén el servicio: `docker stop ms-cuentas`.
2. Repite la consulta del BFF Móvil con token (sección 6.8).
3. Observa la respuesta y revisa `docker logs ms-bff-movil --tail 30`.
4. Vuelve a levantarlo: `docker start ms-cuentas`, espera unos 30 segundos y repite la consulta: debe volver a responder **200**.

Revisa la sección 10 sobre el comportamiento actual en el paso 3.


## 7. Resumen de las comprobaciones

| # | Componente | Prueba | Resultado esperado |
|---|---|---|---|
| 1 | MySQL / Kafka | 6.1 | Base `bancoxyz_batch` creada; broker responde |
| 2 | Config Server | 6.2 | JSON con la configuración del servicio |
| 3 | Eureka | 6.3 | 7 servicios `UP` |
| 4 | Auth Server | 6.4 | Token Bearer, 299 s, 401 con credenciales malas |
| 5 | Batch | 6.5 | 470 / 836 / 397 filas; 409 al repetir; 0 duplicados |
| 6 | Cuentas | 6.6 | Saldo que coincide con la base |
| 7 | Pagos + Clientes (Kafka) | 6.7 | Evento recibido en el log de `ms-clientes` |
| 8 | BFF Móvil | 6.8 | 401 sin token, 200 con token |
| 9 | BFF Web | 6.9 | 401 sin token; autenticado con token |
| 10 | BFF Cajero | 6.10 | 401 sin token, 200 con token |
| 11 | Resiliencia | 6.11 | Respuesta controlada con `ms-cuentas` detenido |

## 8. Detener, reiniciar y limpiar

```bash
docker compose stop            # detiene los contenedores y conserva los datos
docker compose start           # los vuelve a iniciar
docker compose down            # elimina los contenedores y conserva el volumen de MySQL
docker compose down -v         # elimina también los datos de MySQL (empezar de cero)
docker compose up -d --build   # reconstruye y levanta tras cambiar código
```

Usa `docker compose down -v` si quieres repetir las pruebas del batch desde cero: así la base queda vacía y los jobs vuelven a responder 200 en lugar de 409. Si tienes datos de una versión anterior con filas duplicadas, esta es la forma más simple de limpiarlos (los datos se regeneran desde los CSV).

Para reconstruir un solo servicio: `docker compose up -d --build ms-bff-movil`.
