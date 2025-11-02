-- Script de inicialización de la base de datos

-- 1. CREACIÓN DE TABLAS

-- Tabla de Países
CREATE TABLE IF NOT EXISTS countries (
    code VARCHAR(3) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    role VARCHAR(50) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    is_active BOOLEAN DEFAULT TRUE,
    country_code VARCHAR(3) REFERENCES countries(code)
);

-- Tabla de Productos
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    product_status VARCHAR(50) NOT NULL,
    creation_date TIMESTAMP DEFAULT NOW()
);

-- Tabla de Pedidos
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL,
    creation_date TIMESTAMP DEFAULT NOW()
);

-- Tabla de relación Pedidos-Productos
CREATE TABLE IF NOT EXISTS orders_products (
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    amount INTEGER NOT NULL CHECK (amount > 0),
    PRIMARY KEY (order_id, product_id)
);

-- 2. INSERCIÓN DE DATOS (DUMMY DATA)

-- Insertar países de ejemplo
INSERT INTO countries (code, name) VALUES
    ('ES', 'España'),
    ('FR', 'Francia'),
    ('DE', 'Alemania'),
    ('IT', 'Italia'),
    ('PT', 'Portugal'),
    ('GB', 'Reino Unido'),
    ('US', 'Estados Unidos'),
    ('MX', 'México')
ON CONFLICT (code) DO NOTHING;

-- Usuario administrador
-- Email: admin@example.com
-- Password: password
-- Hash BCrypt generado y verificado
INSERT INTO users (full_name, email, password, role, created_at, is_active, country_code) VALUES
    ('Administrator', 'admin@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ROLE_ADMIN', NOW(), true, 'ES')
ON CONFLICT (email) DO NOTHING;

-- Usuario normal
-- Email: user@example.com
-- Password: password
-- Hash BCrypt generado y verificado
INSERT INTO users (full_name, email, password, role, created_at, is_active, country_code) VALUES
    ('John Doe', 'user@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ROLE_USER', NOW(), true, 'US')
ON CONFLICT (email) DO NOTHING;

-- Insertar productos de ejemplo
INSERT INTO products (name, price, product_status, creation_date) VALUES
    ('Laptop Dell XPS 15', 1299.99, 'AVAILABLE', NOW()),
    ('iPhone 15 Pro', 1199.99, 'AVAILABLE', NOW()),
    ('Samsung Galaxy S24', 999.99, 'AVAILABLE', NOW()),
    ('Sony WH-1000XM5 Headphones', 399.99, 'AVAILABLE', NOW()),
    ('Apple Watch Series 9', 499.99, 'AVAILABLE', NOW()),
    ('iPad Air', 699.99, 'AVAILABLE', NOW()),
    ('Microsoft Surface Pro 9', 1099.99, 'AVAILABLE', NOW()),
    ('Nintendo Switch OLED', 349.99, 'AVAILABLE', NOW()),
    ('PlayStation 5', 499.99, 'DISCONTINUED', NOW()),
    ('Xbox Series X', 499.99, 'DISCONTINUED', NOW());