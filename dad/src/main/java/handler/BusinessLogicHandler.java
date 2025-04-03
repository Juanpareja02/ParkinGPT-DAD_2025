package handler;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

public class BusinessLogicHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessLogicHandler.class);
    private final DatabaseService dbService;
    // private final MqttService mqttService; // Inyectar si necesitas enviar comandos MQTT

    // public BusinessLogicHandler(DatabaseService dbService, MqttService mqttService) {
       public BusinessLogicHandler(DatabaseService dbService) {
        this.dbService = dbService;
       // this.mqttService = mqttService;
    }

    public void handlePostSensorData(RoutingContext ctx) {
        try {
            // Decodificar el cuerpo JSON a nuestro DTO.
             final SensorValueInput input = Json.decodeValue(ctx.getBodyAsString(), SensorValueInput.class);

            if (input == null || input.id_sensor == null || input.valor == null) {
                 LOGGER.warn("Received invalid sensor data payload: {}", ctx.getBodyAsString());
                 ctx.response().setStatusCode(400).end("Invalid payload. Required: {\"id_sensor\": \"...\", \"valor\": ...}");
                 return;
            }

            LOGGER.info("Received sensor data: id_sensor={}, valor={}", input.id_sensor, input.valor);

            dbService.saveSensorValue(input)
                .onSuccess(v -> {
                    ctx.response()
                       .setStatusCode(201) // 201 Created (o 200 OK si prefieres)
                       .putHeader("Content-Type", "application/json")
                       .end(new JsonObject().put("message", "Sensor data received successfully").encode());

                    // --- Aquí iría la Lógica de Evaluación Automática ---
                    // Ejemplo:
                    // checkRulesAndTriggerActuators(input);

                })
                .onFailure(err -> {
                    LOGGER.error("Failed to save sensor data for {}: {}", input.id_sensor, err.getMessage());
                    ctx.response().setStatusCode(500).end("Failed to process sensor data: " + err.getMessage());
                });

        } catch (Exception e) {
            LOGGER.error("Error processing POST /api/business/sensorData", e);
             ctx.response().setStatusCode(400).end("Bad Request: " + e.getMessage());
        }
    }

     // --- Implementar aquí los otros handlers de Lógica de Negocio ---
     // handleGetLatestSensorValues, handleGetLatestActuatorStates, etc.

    /*
    private void checkRulesAndTriggerActuators(SensorValueInput data) {
        // 1. Obtener las reglas para este sensor (desde DB o config)
        // 2. Evaluar si 'data.valor' está fuera de rango
        // 3. Si está fuera de rango:
        //    a. Determinar qué actuador(es) controlar y el estado deseado (true/false)
        //    b. Obtener el 'identificador' del actuador y el 'canal_mqtt' del grupo
        //    c. Llamar a mqttService.publishActuatorCommand(topic, actuatorIdentifier, state);
        LOGGER.info("Checking rules for sensor {} with value {}", data.id_sensor, data.valor);
        // Placeholder para la lógica de reglas
        if ("PARKING_SENSOR_01".equals(data.id_sensor)) {
            boolean occupied = data.valor == 1.0; // Asumiendo 1.0 = ocupado, 0.0 = libre
            LOGGER.info("Parking space 1 is now {}", occupied ? "OCCUPIED" : "FREE");
            // Si el servidor necesitara controlar un actuador central:
            // String actuatorId = occupied ? "CENTRAL_DISPLAY_SHOW_OCCUPIED" : "CENTRAL_DISPLAY_SHOW_FREE";
            // String topic = "parking/central/commands"; // O el topic del grupo
            // mqttService.publishActuatorCommand(topic, actuatorId, true);
        }
    }
    */

}