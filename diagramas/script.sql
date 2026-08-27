

CREATE DATABASE IF NOT EXISTS plataforma_citas;
USE plataforma_citas;


-- 1. Tabla base usuarios
CREATE TABLE IF NOT EXISTS usuarios (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    verificado BOOLEAN NOT NULL DEFAULT FALSE,
    token_verificacion VARCHAR(255),
    token_expiracion DATETIME,
    token_recuperacion VARCHAR(255),
    token_recuperacion_expiracion DATETIME,
    rol VARCHAR(20) NOT NULL DEFAULT 'CLIENTE'
    );


-- 2. Tabla clientes (Herencia JOINED)
CREATE TABLE IF NOT EXISTS clientes (
                                        usuario_id BIGINT PRIMARY KEY,
                                        telefono VARCHAR(20),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
    );


-- 3. Tabla especialidades
CREATE TABLE IF NOT EXISTS especialidades (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255)
    );


-- 4. Tabla empleados (Herencia JOINED)
CREATE TABLE IF NOT EXISTS empleados (
                                         usuario_id BIGINT PRIMARY KEY,
                                         especialidad_id BIGINT NOT NULL,
                                         FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (especialidad_id) REFERENCES especialidades(id)
    );


-- 5. Tabla administradores (Herencia JOINED)
CREATE TABLE IF NOT EXISTS administradores (
                                               usuario_id BIGINT PRIMARY KEY,
                                               FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
    );


-- 6. Tabla categorias_servicio
CREATE TABLE IF NOT EXISTS categorias_servicio (
                                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                   nombre VARCHAR(50) NOT NULL,
    descripcion TEXT
    );


-- 7. Tabla servicios
CREATE TABLE IF NOT EXISTS servicios (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         categoria_id BIGINT NOT NULL,
                                         nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    duracion_minutos INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (categoria_id) REFERENCES categorias_servicio(id)
    );


-- 8. Tabla horarios_disponibilidad
CREATE TABLE IF NOT EXISTS horarios_disponibilidad (
                                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                       empleado_id BIGINT NOT NULL,
                                                       fecha DATE NOT NULL,
                                                       hora_inicio TIME NOT NULL,
                                                       hora_fin TIME NOT NULL,
                                                       estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    FOREIGN KEY (empleado_id) REFERENCES empleados(usuario_id) ON DELETE CASCADE
    );


-- 9. Tabla citas
CREATE TABLE IF NOT EXISTS citas (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     cliente_id BIGINT NOT NULL,
                                     empleado_id BIGINT NOT NULL,
                                     servicio_id BIGINT NOT NULL,
                                     fecha DATE NOT NULL,
                                     hora TIME NOT NULL,
                                     estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_modificacion DATETIME,
    detalle_ultimo_cambio VARCHAR(500),
    FOREIGN KEY (cliente_id) REFERENCES clientes(usuario_id),
    FOREIGN KEY (empleado_id) REFERENCES empleados(usuario_id),
    FOREIGN KEY (servicio_id) REFERENCES servicios(id)
    );


-- 10. Tabla intermedia empleado_servicio (Muchos a Muchos)
CREATE TABLE IF NOT EXISTS empleado_servicio (
                                                 empleado_id BIGINT NOT NULL,
                                                 servicio_id BIGINT NOT NULL,
                                                 PRIMARY KEY (empleado_id, servicio_id),
    FOREIGN KEY (empleado_id) REFERENCES empleados(usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (servicio_id) REFERENCES servicios(id) ON DELETE CASCADE
    );


-- 11. Tabla notificaciones
CREATE TABLE IF NOT EXISTS notificaciones (
                                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              usuario_id BIGINT NOT NULL,
    mensaje TEXT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
    );
