# Reglas de procesamiento batch

## Alcance y fechas

Los tres jobs leen sus CSV empaquetados en `src/main/resources/data/`. La fecha se acepta únicamente como `yyyy-MM-dd`, `yyyy/MM/dd`, `dd/MM/yyyy` o `dd-MM-yyyy`. Los formatos día/mes/año se interpretan siempre con día primero. El parser detecta la forma con regex y aplica resolución estricta: fechas inexistentes, vacías, futuras o con otra forma se rechazan como `FECHA_INVALIDA`. El rango calendario válido es desde `0000-01-01` hasta hoy, inclusive (año de cuatro dígitos requerido por el formato).

Todos los textos categóricos se recortan, convierten a minúsculas y se normalizan sin tildes. Los importes se leen con `BigDecimal`, se redondean a dos decimales con `HALF_UP` y se validan después del redondeo. Cada rechazo conserva job, archivo, línea física del CSV, motivo, contenido original y fecha de proceso en `batch_rechazos`; la clave job + archivo + línea hace idempotente el registro.

## Reglas por job

| Job / tabla | Validación y normalización | Motivos de rechazo principales |
|---|---|---|
| Movimientos diarios / `movimientos_diarios` | `cuentaId` debe ser entero positivo. `credito` guarda `+abs(monto)` y `debito` guarda `-abs(monto)`. El monto no puede estar vacío, ser no numérico ni redondear a cero. `tipo` queda normalizado. No se aplica cargo fijo de retiro. | `TIPO_INVALIDO`, `MONTO_INVALIDO`, `CUENTA_INVALIDA`, `FECHA_INVALIDA`, `CSV_MALFORMADO` |
| Estados anuales / `estados_anuales` | `cuenta_id` debe ser entero positivo. `deposito` (con o sin tilde) guarda `+abs(monto)`; `compra`, `retiro` y `pago` guardan `-abs(monto)`. Monto no vacío ni cero. Transacción queda normalizada; descripción vacía se reemplaza por `SIN DESCRIPCION`. | `TIPO_INVALIDO`, `MONTO_INVALIDO`, `CUENTA_INVALIDA`, `FECHA_INVALIDA`, `CSV_MALFORMADO` |
| Interés mensual / `intereses_mensuales` | Tipos `ahorro`, `prestamo`, `hipoteca`; edad entera de 18 a 120; saldo mayor que cero; cuenta positiva; nombre no vacío ni `unknown`. Se persisten saldo base, tasas, interés, saldo final, periodo, archivo y línea. | `TIPO_INVALIDO`, `NOMBRE_INVALIDO`, `EDAD_FUERA_DE_RANGO`, `MONTO_INVALIDO`, `CUENTA_INVALIDA`, `CSV_MALFORMADO` |

## Interés mensual y supuestos

El CSV de entrada contiene un cierre trimestral sin fecha/período. No se capitaliza el trimestre como si fuera un mes: se toma su saldo como base y se calcula interés simple mensual:

```text
tasaMensual = tasaAnual / 12
interesMensual = redondear(saldoBase * tasaAnual / 12, 2, HALF_UP)
saldoFinal = saldoBase + interesMensual
```

Tasas anuales nominales configurables: ahorro `0.02`, préstamo `0.05`, hipoteca `0.05`. Son los valores presentes en el procesamiento previo, reinterpretados como tasas anuales; esta periodicidad es un supuesto de negocio por confirmar frente a la definición contractual del legacy. El interés de ahorro se abona y el de préstamo/hipoteca se devenga aumentando la deuda. El método de capitalización está concentrado en `InteresMensualProcessor.calcularInteresMensual`.

`periodo` se acepta como `yyyy-MM`; si se omite, se usa el mes actual. El período se almacena como parte de la identidad natural de cada interés.

## Idempotencia y reejecución

Cada fila de negocio queda identificada por `(archivo_origen, linea_origen)`; para intereses se incluye además `periodo`. La escritura es `INSERT ... ON DUPLICATE KEY UPDATE`. La instancia del job se identifica con nombre de archivo y SHA-256; el período también identifica el job de intereses. La misma petición completada sin `forzar` responde `409 Conflict`. Una ejecución fallida puede reiniciarse con los mismos parámetros. `forzar=true` añade un identificador de instancia nuevo; el upsert evita duplicar filas.

Cada rechazo se inserta con upsert usando `(job_nombre, archivo_origen, linea_origen)`. `batch.skip-limit` tiene valor inicial 10000 y `batch.max-rechazo-pct` 80. Se conserva el skip de líneas que arrojan `FlatFileParseException`; la tasa de rechazo superior al máximo hace fallar el job.

## Esquema e instalación

Hibernate crea/actualiza las tablas de negocio porque `spring.jpa.hibernate.ddl-auto` está en `update`; `Consultas Tablas Batch - EFT 9.sql` no es fuente de esquema (solo tiene DROP y SELECT). Antes de arrancar esta versión sobre una BD con tablas anteriores, revisar respaldo y ejecutar manualmente `src/main/resources/db/migracion_batch_v2.sql`. El bloque TRUNCATE está comentado: se recomienda habilitarlo solo si se decide regenerar las tres tablas desde cero; el script nunca se ejecuta automáticamente. Hay un bloque alternativo comentado de deduplicación conservando el menor `id`.

## Consultas de comprobación después de ejecutar los jobs

Ejecutar el verificador independiente desde la raíz del módulo:

```powershell
python scripts\verificar_esperados.py
```

Los conteos de `aceptadas` deben coincidir con las tablas y los rechazos por archivo/job con `batch_rechazos`. Si se conserva la información anterior en lugar de truncar, las filas `legacy-existing` no forman parte del total esperado del verificador.

```sql
SELECT archivo_origen, COUNT(*) AS filas
FROM movimientos_diarios
WHERE archivo_origen <> 'legacy-existing'
GROUP BY archivo_origen;

SELECT archivo_origen, COUNT(*) AS filas
FROM estados_anuales
WHERE archivo_origen <> 'legacy-existing'
GROUP BY archivo_origen;

SELECT archivo_origen, periodo, COUNT(*) AS filas
FROM intereses_mensuales
WHERE archivo_origen <> 'legacy-existing'
GROUP BY archivo_origen, periodo;

SELECT job_nombre, archivo_origen, motivo, COUNT(*) AS rechazados
FROM batch_rechazos
GROUP BY job_nombre, archivo_origen, motivo
ORDER BY job_nombre, archivo_origen, motivo;

SELECT archivo_origen, linea_origen, COUNT(*) AS duplicados
FROM movimientos_diarios
GROUP BY archivo_origen, linea_origen
HAVING COUNT(*) > 1;

SELECT archivo_origen, linea_origen, COUNT(*) AS duplicados
FROM estados_anuales
GROUP BY archivo_origen, linea_origen
HAVING COUNT(*) > 1;

SELECT archivo_origen, linea_origen, periodo, COUNT(*) AS duplicados
FROM intereses_mensuales
GROUP BY archivo_origen, linea_origen, periodo
HAVING COUNT(*) > 1;
```
