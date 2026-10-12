-- Migracion manual v2 para bancoxyz_batch. Respaldar la base antes de ejecutar.
-- Aplicar una sola vez y antes de iniciar la nueva version de ms-batch-data.
USE bancoxyz_batch;

CREATE TABLE IF NOT EXISTS batch_rechazos (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    job_nombre VARCHAR(100) NOT NULL,
    archivo_origen VARCHAR(120) NOT NULL,
    linea_origen INT NOT NULL,
    motivo VARCHAR(80) NOT NULL,
    contenido_original TEXT,
    fecha_proceso DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_batch_rechazo_origen UNIQUE (job_nombre, archivo_origen, linea_origen)
);

ALTER TABLE movimientos_diarios
    ADD COLUMN archivo_origen VARCHAR(120) NULL,
    ADD COLUMN linea_origen INT NULL;

ALTER TABLE estados_anuales
    ADD COLUMN archivo_origen VARCHAR(120) NULL,
    ADD COLUMN linea_origen INT NULL;

ALTER TABLE intereses_mensuales
    ADD COLUMN saldo_base DECIMAL(19,2) NULL,
    ADD COLUMN tasa_anual DECIMAL(10,8) NULL,
    ADD COLUMN tasa_mensual DECIMAL(14,10) NULL,
    ADD COLUMN interes_mensual DECIMAL(19,2) NULL,
    ADD COLUMN saldo_final DECIMAL(19,2) NULL,
    ADD COLUMN periodo VARCHAR(7) NULL,
    ADD COLUMN archivo_origen VARCHAR(120) NULL,
    ADD COLUMN linea_origen INT NULL;

-- RECOMENDADO: si los datos anteriores se pueden regenerar desde los CSV,
-- descomentar este bloque para eliminar los duplicados heredados y reprocesar.
-- ADVERTENCIA: TRUNCATE vacia permanentemente esas tres tablas.
-- TRUNCATE TABLE movimientos_diarios;
-- TRUNCATE TABLE estados_anuales;
-- TRUNCATE TABLE intereses_mensuales;

-- Alternativa sin truncar: si se ejecuta este bloque, hacerlo antes de
-- asignar identidades sinteticas y crear los indices UNIQUE. Conserva el menor id.
-- Descomentar solo este bloque O el bloque TRUNCATE, nunca ambos.
/*
DELETE d FROM movimientos_diarios d
JOIN movimientos_diarios menor
  ON d.cuenta_id = menor.cuenta_id
 AND d.fecha = menor.fecha
 AND d.tipo = menor.tipo
 AND d.monto = menor.monto
 AND d.id > menor.id;

DELETE d FROM estados_anuales d
JOIN estados_anuales menor
  ON d.cuenta_id = menor.cuenta_id
 AND d.fecha = menor.fecha
 AND d.transaccion = menor.transaccion
 AND d.monto = menor.monto
 AND d.descripcion <=> menor.descripcion
 AND d.id > menor.id;

DELETE d FROM intereses_mensuales d
JOIN intereses_mensuales menor
  ON d.cuenta_id = menor.cuenta_id
 AND d.nombre = menor.nombre
 AND d.edad = menor.edad
 AND d.tipo = menor.tipo
 AND d.saldo = menor.saldo
 AND d.id > menor.id;
*/

-- Alternativa sin truncar: asigna identidad sintetica a las filas heredadas.
-- No mezclar esos registros heredados con conteos de ejecucion de los CSV.
UPDATE movimientos_diarios
SET archivo_origen = 'legacy-existing', linea_origen = id
WHERE archivo_origen IS NULL OR linea_origen IS NULL;

UPDATE estados_anuales
SET archivo_origen = 'legacy-existing', linea_origen = id
WHERE archivo_origen IS NULL OR linea_origen IS NULL;

UPDATE intereses_mensuales
SET saldo_base = COALESCE(saldo_base, saldo),
    tasa_anual = COALESCE(tasa_anual, 0),
    tasa_mensual = COALESCE(tasa_mensual, 0),
    interes_mensual = COALESCE(interes_mensual, 0),
    saldo_final = COALESCE(saldo_final, saldo),
    periodo = COALESCE(periodo, 'legacy'),
    archivo_origen = COALESCE(archivo_origen, 'legacy-existing'),
    linea_origen = COALESCE(linea_origen, id);

ALTER TABLE movimientos_diarios
    MODIFY archivo_origen VARCHAR(120) NOT NULL,
    MODIFY linea_origen INT NOT NULL,
    ADD CONSTRAINT uk_movimientos_origen UNIQUE (archivo_origen, linea_origen);

ALTER TABLE estados_anuales
    MODIFY archivo_origen VARCHAR(120) NOT NULL,
    MODIFY linea_origen INT NOT NULL,
    ADD CONSTRAINT uk_estados_origen UNIQUE (archivo_origen, linea_origen);

ALTER TABLE intereses_mensuales
    MODIFY saldo_base DECIMAL(19,2) NOT NULL,
    MODIFY tasa_anual DECIMAL(10,8) NOT NULL,
    MODIFY tasa_mensual DECIMAL(14,10) NOT NULL,
    MODIFY interes_mensual DECIMAL(19,2) NOT NULL,
    MODIFY saldo_final DECIMAL(19,2) NOT NULL,
    MODIFY periodo VARCHAR(7) NOT NULL,
    MODIFY archivo_origen VARCHAR(120) NOT NULL,
    MODIFY linea_origen INT NOT NULL,
    ADD CONSTRAINT uk_intereses_origen_periodo
        UNIQUE (archivo_origen, linea_origen, periodo);
