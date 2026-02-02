CREATE DATABASE IF NOT EXISTS aseguradora_db;
USE aseguradora_db;

-- ==========================================
-- 1. CREACIÓN DE TABLAS (Estructura)
-- ==========================================

-- Tabla USUARIOS
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(50) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    movil VARCHAR(15),
    direccion VARCHAR(255),
    rol VARCHAR(20),
    activo BOOLEAN,
    fecha_registro DATETIME,
    
    -- Campos de seguridad (2FA y Tokens)
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    confirmation_token VARCHAR(255),
    reset_token VARCHAR(255)
);

-- Tabla TIPOS_SEGURO
CREATE TABLE IF NOT EXISTS tipos_seguro (
    id_tipo BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    precio_base DECIMAL(19,2),
    descripcion TEXT
);

-- Tabla SEGUROS 
CREATE TABLE IF NOT EXISTS seguros (
    id_seguro BIGINT AUTO_INCREMENT PRIMARY KEY,
    num_poliza VARCHAR(20) NOT NULL UNIQUE,
    fecha_inicio DATE NOT NULL,
    fecha_renovacion DATE NOT NULL,
    prima_anual DECIMAL(19,2) NOT NULL,
    datos_especificos TEXT,
    estado VARCHAR(20),
    id_usuario BIGINT NOT NULL,
    id_tipo BIGINT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_tipo) REFERENCES tipos_seguro(id_tipo)
);

-- Tabla FACTURAS
CREATE TABLE IF NOT EXISTS facturas (
    id_factura BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_emision DATE NOT NULL,
    importe DECIMAL(19,2) NOT NULL,
    concepto VARCHAR(100),
    estado VARCHAR(20),
    id_seguro BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    FOREIGN KEY (id_seguro) REFERENCES seguros(id_seguro),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

-- Tabla SINIESTROS
-- CORRECCIÓN IMPORTANTE: Añadido id_usuario para coincidir con tu Java
CREATE TABLE IF NOT EXISTS siniestros (
    id_siniestro BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_suceso DATE,
    descripcion VARCHAR(255),
    estado VARCHAR(255),
    resolucion VARCHAR(255),
    id_seguro BIGINT,
    id_usuario BIGINT NOT NULL, 
    FOREIGN KEY (id_seguro) REFERENCES seguros(id_seguro),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

-- ==========================================
-- 2. INSERCIÓN DE DATOS (Semilla)
-- ==========================================

-- A. USUARIOS
-- Admin (Pass: 1234)
INSERT INTO usuarios (id_usuario, nombre_completo, correo, password, rol, activo, fecha_registro, movil) 
VALUES (1, 'Admin Principal', 'admin@aseguradora.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAtepbyJscMnZlrMysMEw.3WWlD2', 'ADMIN', 1, NOW(), '600111222');

-- Cliente (Pass: 1234)
INSERT INTO usuarios (id_usuario, nombre_completo, correo, password, rol, activo, fecha_registro, movil) 
VALUES (2, 'Cliente Ejemplo', 'cliente@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAtepbyJscMnZlrMysMEw.3WWlD2', 'USER', 1, NOW(), '600333444');


-- B. TIPOS DE SEGURO (LISTA AMPLIADA)
INSERT INTO tipos_seguro (id_tipo, nombre, precio_base, descripcion) VALUES 
(1, 'Seguro de Hogar Plus', 120.00, 'Protección contra robos, incendios y daños por agua.'),
(2, 'Seguro de Coche Total', 450.00, 'Todo riesgo con franquicia incluidos daños propios.'),
(3, 'Seguro de Salud', 45.50, 'Asistencia médica primaria y especialistas sin copago.'),
(4, 'Seguro de Vida', 200.00, 'Tranquilidad financiera para tu familia.'),
(5, 'Seguro de Moto', 90.00, 'Cobertura obligatoria y asistencia en viaje km 0.'),
(6, 'Seguro de Viaje', 30.00, 'Cobertura médica y pérdida de equipaje en el extranjero.'),
(7, 'Seguro de Mascotas', 15.00, 'Veterinario, vacunas y responsabilidad civil.'),
(8, 'Seguro de Decesos', 12.00, 'Gestión completa de servicios funerarios y traslados.');


-- C. SEGUROS CONTRATADOS (EJEMPLOS)
-- 1. Coche (Ya existía)
INSERT INTO seguros (id_seguro, num_poliza, fecha_inicio, fecha_renovacion, prima_anual, datos_especificos, estado, id_usuario, id_tipo) 
VALUES (1, 'POL-CAR-998877', '2024-01-01', '2025-01-01', 350.50, 'Matrícula: 1234-BBB, Modelo: Audi A3', 'ACTIVO', 2, 2);

-- 2. Salud (NUEVO para que veas más datos)
INSERT INTO seguros (id_seguro, num_poliza, fecha_inicio, fecha_renovacion, prima_anual, datos_especificos, estado, id_usuario, id_tipo) 
VALUES (2, 'POL-SAL-554433', '2024-03-15', '2025-03-15', 546.00, 'Beneficiarios: Titular y cónyuge', 'ACTIVO', 2, 3);


-- D. FACTURAS
INSERT INTO facturas (id_factura, fecha_emision, importe, concepto, estado, id_usuario, id_seguro) 
VALUES (1, '2024-06-15', 350.50, 'Renovación Anual - Póliza Coche', 'PAGADA', 2, 1);

INSERT INTO facturas (id_factura, fecha_emision, importe, concepto, estado, id_usuario, id_seguro) 
VALUES (2, '2024-03-15', 45.50, 'Cuota Mensual - Seguro Salud', 'PENDIENTE', 2, 2);