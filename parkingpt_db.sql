CREATE DATABASE IF NOT EXISTS parkingpt_db;
USE parkingpt_db;

CREATE TABLE IF NOT EXISTS groups (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(255),
  canal_mqtt VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS devices (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(255),
  id_grupo INT,
  FOREIGN KEY (id_grupo) REFERENCES groups(id)
);

CREATE TABLE IF NOT EXISTS sensors (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(255),
  tipo VARCHAR(255),
  identificador VARCHAR(255),
  id_dispositivo INT,
  FOREIGN KEY (id_dispositivo) REFERENCES devices(id)
);

CREATE TABLE IF NOT EXISTS actuators (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(255),
  tipo VARCHAR(255),
  identificador VARCHAR(255),
  id_dispositivo INT,
  FOREIGN KEY (id_dispositivo) REFERENCES devices(id)
);

CREATE TABLE IF NOT EXISTS sensor_values (
  id INT AUTO_INCREMENT PRIMARY KEY,
  id_sensor INT,
  valor FLOAT,
  fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_sensor) REFERENCES sensors(id)
);

CREATE TABLE IF NOT EXISTS actuator_states (
  id INT AUTO_INCREMENT PRIMARY KEY,
  id_actuator INT,
  estado BOOLEAN,
  fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_actuator) REFERENCES actuators(id)
);

CREATE TABLE IF NOT EXISTS sensor_ranges (
  id_sensor INT PRIMARY KEY,
  min_value FLOAT NOT NULL,
  max_value FLOAT NOT NULL,
  FOREIGN KEY (id_sensor) REFERENCES sensors(id)
);

INSERT INTO groups (nombre, canal_mqtt) VALUES ('Grupo 1', 'grupo_1/canal_actuador');

INSERT INTO devices (nombre, id_grupo) VALUES ('Dispositivo 1', 1);

INSERT INTO sensors (nombre, tipo, identificador, id_dispositivo) VALUES ('Sensor 1', 'ultrasonico', 'sensor_1', 1);

INSERT INTO actuators (nombre, tipo, identificador, id_dispositivo) VALUES ('LED Rojo', 'led', 'act_1', 1);

INSERT INTO sensor_ranges (id_sensor, min_value, max_value) VALUES (1, 10.0, 30.0);
