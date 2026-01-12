INSERT INTO usuarios (nombre_completo, correo, password, rol, activo) 
VALUES ('Admin', 'admin@aseguradora.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAtepbyJscMnZlrMysMEw.3WWlD2', 'ADMIN', 1);

INSERT INTO tipos_seguro (nombre, precio_base, descripcion) 
VALUES ('Seguro de Hogar Plus', 120.00, 'Protección total');