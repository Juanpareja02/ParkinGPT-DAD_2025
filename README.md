# ParkinGPT – Sistema de Gestión de Aparcamientos Inteligente 🚗

ParkinGPT es una aplicación distribuida para la gestión inteligente de plazas de aparcamiento usando sensores, actuadores, nodos ESP32 y comunicación MQTT. Este proyecto ha sido desarrollado como parte de la asignatura de Diseño de Aplicaciones Distribuidas (DAD).

---

## 📌 Funcionalidades

- Registro de sensores y actuadores vinculados a dispositivos.
- Recepción y almacenamiento de datos de sensores (ultrasonido).
- Evaluación automática de rangos válidos y control de actuadores (ej. LED).
- Comunicación bidireccional mediante MQTT.
- API RESTful para CRUD y lógica de negocio.
- Consultas específicas por grupo, sensor y actuador.

---

## 🧩 Estructura del Proyecto

```
parkingtp/
├── mqtt/                 # Cliente MQTT (suscribe y publica)
├── parkingpt/            # Controlador principal del sistema
├── vertx/                # Endpoints REST de CRUD y lógica de negocio
├── resources/
│   └── schema.sql        # Script de creación de la base de datos
├── README.md
└── pom.xml               # Configuración del proyecto Maven
```

---

## 🛠️ Tecnologías usadas

- Java + Vert.x
- MySQL
- MQTT (Mosquitto)
- ESP32 (simulado o real)
- Postman (para pruebas REST)

---

## ⚙️ Instalación y despliegue

1. **Clona el repositorio:**
```bash
git clone https://github.com/Juanpareja02/ParkinGPT-DAD_2025.git
```

2. **Configura la base de datos:**
- Lanza el script `schema.sql` sobre una instancia MySQL.
- Define las variables de entorno `DB_HOST`, `DB_USER` y `DB_PASS` con los datos de conexión (por defecto `localhost`, `root` y `Gratis`).

3. **Levanta el broker MQTT:**
```bash
docker run -it -p 1883:1883 eclipse-mosquitto
```

4. **Compila y ejecuta:**
```bash
mvn clean compile exec:java -Dexec.mainClass=parkingpt.ParkingController
```

### Variables de entorno

- `DB_HOST`: host de la base de datos (por defecto `localhost`).
- `DB_USER`: usuario de conexión (por defecto `root`).
- `DB_PASS`: contraseña de conexión (por defecto `Gratis`).

---

## 🔌 Endpoints REST principales

### 📋 CRUD (puerto 8088)
- `/api/sensors`, `/api/actuators`, `/api/devices`, `/api/groups`, `/api/users`
- Métodos soportados: `GET`, `POST`, `PUT`, `DELETE`

### 🤖 Lógica de negocio (puerto 8090)
- `POST /api/business/sensorData`
- `GET /api/business/sensorValues/:id_sensor/latest`
- `GET /api/business/actuatorStates/:id_actuator/latest`
- `GET /api/business/group/:id_grupo/sensorValues/latest`
- `GET /api/business/group/:id_grupo/actuatorStates/latest`

---

## 📡 Tópicos MQTT utilizados

| Propósito           | Tópico ejemplo              |
|--------------------|-----------------------------|
| Recepción sensores | `grupo_1/canal_sensor`       |
| Control actuadores | `grupo_1/canal_actuador`     |

Los tópicos se obtienen desde la base de datos (`groups.canal_mqtt`).

---

## 📦 Postman

El archivo `ParkinGPT.postman_collection.json` contiene todos los endpoints listos para probar. (Asegúrate de tener los puertos activos y los datos insertados en BD antes de lanzar consultas).

---

## 🧠 Autores y créditos

- Juan Álvaro Pareja – Ingeniería Informática de Computadores (Universidad de Sevilla)
- Marcos Guisado – Ingeniería Informática de Computadores (Universidad de Sevilla)
- Hector Bartolomé – Ingeniería Informática de Computadores (Universidad de Sevilla)

---

## 🏁 Pendientes / Mejoras futuras

- Extraer rangos dinámicamente desde la base de datos.
- Añadir autenticación de usuarios.
- Dashboard web de visualización.

---

¡Gracias por visitar ParkinGPT! Aparcar nunca fue tan inteligente 🚦

