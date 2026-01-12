-- 1. Insertar el USUARIO (La contraseña es: 1234)
-- Nota: La contraseña encriptada abajo equivale a "1234"
INSERT INTO usuarios (id_usuario, nombre_completo, correo, password, rol, activo) 
VALUES (1, 'Admin Principal', 'admin@aseguradora.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAtepbyJscMnZlrMysMEw.3WWlD2', 'ADMIN', 1);

-- 2. Insertar el TIPO DE SEGURO
INSERT INTO tipos_seguro (id_tipo, nombre, precio_base, descripcion) 
VALUES (1, 'Seguro de Hogar Plus', 120.00, 'Protección contra robos, incendios y daños por agua.');

-- 3. Insertar el SEGURO (Vinculado al Usuario 1 y al Tipo 1)
INSERT INTO seguros (id_seguro, num_poliza, fecha_inicio, fecha_renovacion, prima_anual, datos_especificos, estado, usuario_id, tipo_seguro_id) 
VALUES (1, 'POL-998877', '2024-01-01', '2025-01-01', 350.50, 'Matrícula: 1234-BBB, Modelo: Audi A3', 'ACTIVO', 1, 1);

-- 4. Insertar la FACTURA (Vinculada al Usuario 1 y al Seguro 1)
INSERT INTO facturas (id_factura, fecha_emision, importe, concepto, estado, usuario_id, seguro_id) 
VALUES (1, '2024-06-15', 350.50, 'Renovación Anual - Póliza Coche', 'PAGADA', 1, 1);