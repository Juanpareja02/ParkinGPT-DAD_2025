-- ---------------------------
-- ParkinGPT Database Schema
-- ---------------------------

-- 1. (Opcional) Eliminar la base de datos existente para partir de cero
DROP DATABASE IF EXISTS parkingpt_db;

-- 2. Crear base de datos y usarla
CREATE DATABASE parkingpt_db;
USE parkingpt_db;

-- 3. TABLAS

-- Usuarios
CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL,
  usuario VARCHAR(50) NOT NULL,
  contraseña VARCHAR(50) NOT NULL,
  matricula VARCHAR(100) UNIQUE
);

-- Grupos
CREATE TABLE groups (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  canal_mqtt VARCHAR(255) UNIQUE
);

-- Dispositivos
CREATE TABLE devices (
  id INT AUTO_INCREMENT PRIMARY KEY,
  plaza VARCHAR(100) NOT NULL,
  id_grupo INT,
  FOREIGN KEY (id_grupo) REFERENCES groups(id) ON DELETE SET NULL
);

-- Sensores
CREATE TABLE sensors (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  tipo VARCHAR(50),
  identificador VARCHAR(100) UNIQUE NOT NULL,
  id_dispositivo INT,
  FOREIGN KEY (id_dispositivo) REFERENCES devices(id) ON DELETE CASCADE
);

-- Actuadores
CREATE TABLE actuators (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  tipo VARCHAR(50),
  identificador VARCHAR(100) UNIQUE NOT NULL,
  id_dispositivo INT,
  FOREIGN KEY (id_dispositivo) REFERENCES devices(id) ON DELETE CASCADE
);

-- Valores de sensor
CREATE TABLE sensor_values (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_sensor INT NOT NULL,
  valor FLOAT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_sensor) REFERENCES sensors(id) ON DELETE CASCADE
);

-- Estados de actuador
CREATE TABLE actuator_states (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_actuator INT NOT NULL,
  estado BOOLEAN,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_actuator) REFERENCES actuators(id) ON DELETE CASCADE
);

-- Rangos permitidos por sensor
CREATE TABLE sensor_ranges (
  id_sensor INT PRIMARY KEY,
  min_value FLOAT NOT NULL,
  max_value FLOAT NOT NULL,
  FOREIGN KEY (id_sensor) REFERENCES sensors(id) ON DELETE CASCADE
);

-- 4. DATOS INICIALES DE EJEMPLO

-- Grupo
INSERT INTO groups (id, nombre, canal_mqtt)
VALUES (1, 'Grupo 1', 'grupo_1/canal_sensor');

-- Dispositivo
INSERT INTO devices (id, plaza, id_grupo)
VALUES (1, 'Plaza A1', 1);

-- Sensor
INSERT INTO sensors (id, nombre, tipo, identificador, id_dispositivo)
VALUES (1, 'Sensor A1', 'ultrasonico', 'sensor_1', 1);

-- Actuador rojo
INSERT INTO actuators (id, nombre, tipo, identificador, id_dispositivo)
VALUES (1, 'LED Rojo', 'led', 'act_1', 1);

-- Actuador verde (opcional)
INSERT INTO actuators (id, nombre, tipo, identificador, id_dispositivo)
VALUES (2, 'LED Verde', 'led', 'act_2', 1);

-- Rangos permitidos
INSERT INTO sensor_ranges (id_sensor, min_value, max_value)
VALUES (1, 10.0, 30.0)
ON DUPLICATE KEY UPDATE min_value = VALUES(min_value), max_value = VALUES(max_value);

-- Valores iniciales de sensor (opcional)
INSERT INTO sensor_values (id_sensor, valor)
VALUES (1, 15.0),(1, 25.0),(1, 35.0);

-- Estados iniciales de actuador (opcional)
INSERT INTO actuator_states (id_actuator, estado)
VALUES (1, TRUE),(2, FALSE);
