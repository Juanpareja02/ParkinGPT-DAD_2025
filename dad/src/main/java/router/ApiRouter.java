package router;

import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class ApiRouter {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApiRouter.class);

    public static Router configureRouter(Vertx vertx, CrudHandler crudHandler, BusinessLogicHandler businessLogicHandler) {
        Router router = Router.router(vertx);

        // Middleware para parsear el cuerpo de las solicitudes POST/PUT
        router.route().handler(BodyHandler.create());

        // --- Rutas CRUD (Ejemplos) ---
        // Necesitarás implementar los métodos correspondientes en CrudHandler
        LOGGER.info("Configuring CRUD routes...");
        router.post("/api/sensors").handler(crudHandler::createSensor);
        router.get("/api/sensors/:id").handler(crudHandler::getSensor);
        router.get("/api/sensors").handler(crudHandler::getAllSensors);
        router.put("/api/sensors/:id").handler(crudHandler::updateSensor);
        router.delete("/api/sensors/:id").handler(crudHandler::deleteSensor);
        // ... (Rutas CRUD para Actuators, Devices, Groups, SensorValues, ActuatorStates) ...

        // --- Rutas de Lógica de Negocio ---
        LOGGER.info("Configuring Business Logic routes...");
        router.post("/api/business/sensorData").handler(businessLogicHandler::handlePostSensorData);
        // ... (Rutas GET para /latest) ...
        // router.get("/api/business/sensorValues/:id_sensor/latest").handler(businessLogicHandler::handleGetLatestSensorValues);
        // router.get("/api/business/actuatorStates/:id_actuator/latest").handler(businessLogicHandler::handleGetLatestActuatorStates);
        // router.get("/api/business/group/:id_grupo/sensorValues/latest").handler(businessLogicHandler::handleGetGroupLatestSensorValues);
        // router.get("/api/business/group/:id_grupo/actuatorStates/latest").handler(businessLogicHandler::handleGetGroupLatestActuatorStates);


        LOGGER.info("API Router configured successfully.");
        return router;
    }
}
