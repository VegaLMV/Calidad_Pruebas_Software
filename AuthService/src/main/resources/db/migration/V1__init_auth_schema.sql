CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE usuarios (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          username VARCHAR(50) UNIQUE NOT NULL,
                          email VARCHAR(150) UNIQUE NOT NULL,
                          password_hash VARCHAR(255) NOT NULL,

                          requiere_cambio_password BOOLEAN NOT NULL DEFAULT FALSE,
                          mfa_habilitado BOOLEAN NOT NULL DEFAULT FALSE,

                          ultimo_login TIMESTAMP NULL,
                          intentos_fallidos SMALLINT NOT NULL DEFAULT 0,
                          bloqueado_hasta TIMESTAMP NULL,

                          creado_por UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
                          fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          modificado_por UUID NULL,
                          fecha_modificacion TIMESTAMP NULL,

                          estado_registro VARCHAR(15) NOT NULL DEFAULT 'ACTIVO',

                          CONSTRAINT chk_usuarios_estado_registro
                              CHECK (estado_registro IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE roles (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       nombre VARCHAR(50) UNIQUE NOT NULL,
                       descripcion VARCHAR(255) NULL,
                       es_sistema BOOLEAN NOT NULL DEFAULT FALSE,

                       creado_por UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
                       fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       modificado_por UUID NULL,
                       fecha_modificacion TIMESTAMP NULL,

                       estado_registro VARCHAR(15) NOT NULL DEFAULT 'ACTIVO',

                       CONSTRAINT chk_roles_estado_registro
                           CHECK (estado_registro IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE permisos (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          codigo VARCHAR(100) UNIQUE NOT NULL,
                          modulo VARCHAR(50) NOT NULL,
                          descripcion VARCHAR(255) NULL,

                          creado_por UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
                          fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          modificado_por UUID NULL,
                          fecha_modificacion TIMESTAMP NULL,

                          estado_registro VARCHAR(15) NOT NULL DEFAULT 'ACTIVO',

                          CONSTRAINT chk_permisos_estado_registro
                              CHECK (estado_registro IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE rol_permisos (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                              rol_id UUID NOT NULL,
                              permiso_id UUID NOT NULL,

                              creado_por UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
                              fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              modificado_por UUID NULL,
                              fecha_modificacion TIMESTAMP NULL,

                              estado_registro VARCHAR(15) NOT NULL DEFAULT 'ACTIVO',

                              CONSTRAINT fk_rol_permisos_rol
                                  FOREIGN KEY (rol_id) REFERENCES roles(id),

                              CONSTRAINT fk_rol_permisos_permiso
                                  FOREIGN KEY (permiso_id) REFERENCES permisos(id),

                              CONSTRAINT uq_rol_permiso
                                  UNIQUE (rol_id, permiso_id),

                              CONSTRAINT chk_rol_permisos_estado_registro
                                  CHECK (estado_registro IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE usuario_roles (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                               usuario_id UUID NOT NULL,
                               rol_id UUID NOT NULL,
                               fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               creado_por UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
                               fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               modificado_por UUID NULL,
                               fecha_modificacion TIMESTAMP NULL,

                               estado_registro VARCHAR(15) NOT NULL DEFAULT 'ACTIVO',

                               CONSTRAINT fk_usuario_roles_usuario
                                   FOREIGN KEY (usuario_id) REFERENCES usuarios(id),

                               CONSTRAINT fk_usuario_roles_rol
                                   FOREIGN KEY (rol_id) REFERENCES roles(id),

                               CONSTRAINT uq_usuario_rol
                                   UNIQUE (usuario_id, rol_id),

                               CONSTRAINT chk_usuario_roles_estado_registro
                                   CHECK (estado_registro IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE sesiones (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          usuario_id UUID NOT NULL,
                          refresh_token_hash VARCHAR(255) NOT NULL,
                          ip_origen VARCHAR(45) NULL,
                          user_agent VARCHAR(255) NULL,
                          fecha_expiracion TIMESTAMP NOT NULL,
                          revocado BOOLEAN NOT NULL DEFAULT FALSE,

                          creado_por UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
                          fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          modificado_por UUID NULL,
                          fecha_modificacion TIMESTAMP NULL,

                          estado_registro VARCHAR(15) NOT NULL DEFAULT 'ACTIVO',

                          CONSTRAINT fk_sesiones_usuario
                              FOREIGN KEY (usuario_id) REFERENCES usuarios(id),

                          CONSTRAINT chk_sesiones_estado_registro
                              CHECK (estado_registro IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE auth_audit_log (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                usuario_id UUID NULL,
                                tipo_evento VARCHAR(50) NOT NULL,
                                detalle JSONB NULL,
                                ip_origen VARCHAR(45) NULL,

                                creado_por UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
                                fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                modificado_por UUID NULL,
                                fecha_modificacion TIMESTAMP NULL,

                                estado_registro VARCHAR(15) NOT NULL DEFAULT 'ACTIVO',

                                CONSTRAINT fk_auth_audit_log_usuario
                                    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),

                                CONSTRAINT chk_auth_audit_log_tipo_evento
                                    CHECK (tipo_evento IN (
                                                           'LOGIN_EXITOSO',
                                                           'LOGIN_FALLIDO',
                                                           'CAMBIO_PASSWORD',
                                                           'DESBLOQUEO_CUENTA',
                                                           'TOKEN_REVOCADO'
                                        )),

                                CONSTRAINT chk_auth_audit_log_estado_registro
                                    CHECK (estado_registro IN ('ACTIVO', 'INACTIVO'))
);

CREATE INDEX idx_usuarios_username ON usuarios(username);
CREATE INDEX idx_usuarios_email ON usuarios(email);

CREATE INDEX idx_roles_nombre ON roles(nombre);
CREATE INDEX idx_permisos_codigo ON permisos(codigo);

CREATE INDEX idx_rol_permisos_rol_id ON rol_permisos(rol_id);
CREATE INDEX idx_rol_permisos_permiso_id ON rol_permisos(permiso_id);

CREATE INDEX idx_usuario_roles_usuario_id ON usuario_roles(usuario_id);
CREATE INDEX idx_usuario_roles_rol_id ON usuario_roles(rol_id);

CREATE INDEX idx_sesiones_usuario_id ON sesiones(usuario_id);
CREATE INDEX idx_sesiones_refresh_token_hash ON sesiones(refresh_token_hash);

CREATE INDEX idx_auth_audit_log_usuario_id ON auth_audit_log(usuario_id);
CREATE INDEX idx_auth_audit_log_tipo_evento ON auth_audit_log(tipo_evento);
CREATE INDEX idx_auth_audit_log_fecha_creacion ON auth_audit_log(fecha_creacion);