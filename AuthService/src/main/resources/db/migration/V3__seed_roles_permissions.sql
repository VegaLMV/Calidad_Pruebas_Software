INSERT INTO roles (nombre, descripcion, es_sistema, creado_por)
VALUES
    ('ROLE_ADMIN', 'Administrador principal del sistema', TRUE, '00000000-0000-0000-0000-000000000000'),
    ('ROLE_CAJERO', 'Usuario encargado de caja y pagos', TRUE, '00000000-0000-0000-0000-000000000000'),
    ('ROLE_MOZO', 'Usuario encargado de toma de pedidos en salón', TRUE, '00000000-0000-0000-0000-000000000000'),
    ('ROLE_CLIENTE', 'Cliente registrado del restaurante', FALSE, '00000000-0000-0000-0000-000000000000')
    ON CONFLICT (nombre) DO NOTHING;

INSERT INTO permisos (codigo, modulo, descripcion, creado_por)
VALUES
    ('auth:user:create', 'AUTH', 'Crear usuarios del sistema', '00000000-0000-0000-0000-000000000000'),
    ('auth:user:read', 'AUTH', 'Consultar usuarios del sistema', '00000000-0000-0000-0000-000000000000'),
    ('auth:user:update', 'AUTH', 'Actualizar usuarios del sistema', '00000000-0000-0000-0000-000000000000'),
    ('auth:role:assign', 'AUTH', 'Asignar roles a usuarios', '00000000-0000-0000-0000-000000000000'),
    ('auth:permission:read', 'AUTH', 'Consultar permisos del sistema', '00000000-0000-0000-0000-000000000000'),

    ('customer:create', 'CUSTOMER', 'Crear clientes', '00000000-0000-0000-0000-000000000000'),
    ('customer:read', 'CUSTOMER', 'Consultar clientes', '00000000-0000-0000-0000-000000000000'),
    ('customer:update', 'CUSTOMER', 'Actualizar clientes', '00000000-0000-0000-0000-000000000000'),

    ('menu:create', 'MENU', 'Crear productos del menú', '00000000-0000-0000-0000-000000000000'),
    ('menu:read', 'MENU', 'Consultar menú', '00000000-0000-0000-0000-000000000000'),
    ('menu:update', 'MENU', 'Actualizar productos del menú', '00000000-0000-0000-0000-000000000000'),
    ('menu:price:update', 'MENU', 'Actualizar precios del menú', '00000000-0000-0000-0000-000000000000'),

    ('order:create', 'ORDER', 'Crear pedidos', '00000000-0000-0000-0000-000000000000'),
    ('order:read', 'ORDER', 'Consultar pedidos', '00000000-0000-0000-0000-000000000000'),
    ('order:update', 'ORDER', 'Actualizar pedidos', '00000000-0000-0000-0000-000000000000'),
    ('order:cancel', 'ORDER', 'Cancelar pedidos', '00000000-0000-0000-0000-000000000000'),

    ('delivery:assign', 'ORDER', 'Asignar repartidor a delivery', '00000000-0000-0000-0000-000000000000'),

    ('payment:create', 'PAYMENT', 'Registrar pagos', '00000000-0000-0000-0000-000000000000'),
    ('payment:read', 'PAYMENT', 'Consultar pagos', '00000000-0000-0000-0000-000000000000'),
    ('payment:cancel', 'PAYMENT', 'Anular pagos', '00000000-0000-0000-0000-000000000000'),
    ('invoice:issue', 'PAYMENT', 'Emitir comprobantes electrónicos', '00000000-0000-0000-0000-000000000000'),
    ('credit-note:issue', 'PAYMENT', 'Emitir notas de crédito', '00000000-0000-0000-0000-000000000000')
    ON CONFLICT (codigo) DO NOTHING;

-- ROLE_ADMIN recibe todos los permisos
INSERT INTO rol_permisos (rol_id, permiso_id, creado_por)
SELECT r.id, p.id, '00000000-0000-0000-0000-000000000000'
FROM roles r
         CROSS JOIN permisos p
WHERE r.nombre = 'ROLE_ADMIN'
    ON CONFLICT (rol_id, permiso_id) DO NOTHING;

-- ROLE_CAJERO
INSERT INTO rol_permisos (rol_id, permiso_id, creado_por)
SELECT r.id, p.id, '00000000-0000-0000-0000-000000000000'
FROM roles r
         JOIN permisos p ON p.codigo IN (
                                         'customer:read',
                                         'menu:read',
                                         'order:read',
                                         'order:update',
                                         'payment:create',
                                         'payment:read',
                                         'payment:cancel',
                                         'invoice:issue',
                                         'credit-note:issue'
    )
WHERE r.nombre = 'ROLE_CAJERO'
    ON CONFLICT (rol_id, permiso_id) DO NOTHING;

-- ROLE_MOZO
INSERT INTO rol_permisos (rol_id, permiso_id, creado_por)
SELECT r.id, p.id, '00000000-0000-0000-0000-000000000000'
FROM roles r
         JOIN permisos p ON p.codigo IN (
                                         'customer:create',
                                         'customer:read',
                                         'menu:read',
                                         'order:create',
                                         'order:read',
                                         'order:update',
                                         'order:cancel'
    )
WHERE r.nombre = 'ROLE_MOZO'
    ON CONFLICT (rol_id, permiso_id) DO NOTHING;

-- ROLE_CLIENTE
INSERT INTO rol_permisos (rol_id, permiso_id, creado_por)
SELECT r.id, p.id, '00000000-0000-0000-0000-000000000000'
FROM roles r
         JOIN permisos p ON p.codigo IN (
                                         'customer:read',
                                         'customer:update',
                                         'menu:read',
                                         'order:create',
                                         'order:read',
                                         'payment:create',
                                         'payment:read'
    )
WHERE r.nombre = 'ROLE_CLIENTE'
    ON CONFLICT (rol_id, permiso_id) DO NOTHING;