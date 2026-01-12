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
    fecha_registro DATETIME
);

-- Tabla TIPOS_SEGURO
CREATE TABLE IF NOT EXISTS tipos_seguro (
    id_tipo BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    precio_base DECIMAL(19,2),
    descripcion TEXT
);

-- Tabla SEGUROS 
-- IMPORTANTE: Aquí definimos 'id_usuario' y 'id_tipo' para que coincidan con Java
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
CREATE TABLE IF NOT EXISTS siniestros (
    id_siniestro BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_suceso DATE,
    descripcion VARCHAR(255),
    estado VARCHAR(255),
    resolucion VARCHAR(255),
    id_seguro BIGINT,
    FOREIGN KEY (id_seguro) REFERENCES seguros(id_seguro)
);

-- Tabla TOKENS_SEGURIDAD
CREATE TABLE IF NOT EXISTS tokens_seguridad (
    id_token BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    id_usuario BIGINT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

-- ==========================================
-- 2. INSERCIÓN DE DATOS (Semilla)
-- ==========================================

-- Usuario ADMIN (Pass: 1234)
INSERT INTO usuarios (id_usuario, nombre_completo, correo, password, rol, activo, fecha_registro) 
VALUES (1, 'Admin Principal', 'admin@aseguradora.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAtepbyJscMnZlrMysMEw.3WWlD2', 'ADMIN', 1, NOW());

-- Usuario CLIENTE (Pass: 1234)
INSERT INTO usuarios (id_usuario, nombre_completo, correo, password, rol, activo, fecha_registro) 
VALUES (2, 'Cliente Ejemplo', 'cliente@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAtepbyJscMnZlrMysMEw.3WWlD2', 'USER', 1, NOW());

-- Tipos de Seguro
INSERT INTO tipos_seguro (id_tipo, nombre, precio_base, descripcion) 
VALUES (1, 'Seguro de Hogar Plus', 120.00, 'Protección contra robos, incendios y daños por agua.');

INSERT INTO tipos_seguro (id_tipo, nombre, precio_base, descripcion) 
VALUES (2, 'Seguro de Coche Total', 450.00, 'Todo riesgo con franquicia.');

-- Seguro de ejemplo (Vinculado al usuario 2 y tipo 2)
INSERT INTO seguros (id_seguro, num_poliza, fecha_inicio, fecha_renovacion, prima_anual, datos_especificos, estado, id_usuario, id_tipo) 
VALUES (1, 'POL-998877', '2024-01-01', '2025-01-01', 350.50, 'Matrícula: 1234-BBB, Modelo: Audi A3', 'ACTIVO', 2, 2);

-- Factura de ejemplo
INSERT INTO facturas (id_factura, fecha_emision, importe, concepto, estado, id_usuario, id_seguro) 
VALUES (1, '2024-06-15', 350.50, 'Renovación Anual - Póliza Coche', 'PAGADA', 2, 1);