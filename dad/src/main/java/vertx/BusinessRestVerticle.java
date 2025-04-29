package vertx;

import com.google.gson.Gson;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class BusinessRestVerticle extends AbstractVerticle {

    private final Gson gson = new Gson();

    @Override
    public void start(Promise<Void> startFuture) {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        // 1. Recibir datos del sensor
        router.post("/api/business/sensorData").handler(ctx -> {
            JsonObject body = ctx.getBodyAsJson();
            String idSensor = body.getString("id_sensor");
            float valor = body.getFloat("valor");

            // TODO: guardar en BD (tabla SensorValue)
            // TODO: consultar rango del sensor
            // TODO: si fuera de rango, publicar MQTT para activar/desactivar

            ctx.response().setStatusCode(200).end("Sensor " + idSensor + " recibió valor: " + valor);
        });

        // 2. Últimos 10 valores del sensor
        router.get("/api/business/sensorValues/:id_sensor/latest").handler(ctx -> {
            String idSensor = ctx.pathParam("id_sensor");

            // TODO: consultar últimos 10 valores en la tabla SensorValue
            ctx.response().putHeader("Content-Type", "application/json");
            ctx.response().end("[VALORES_SENSOR_SIMULADOS]");
        });

        // 3. Últimos 10 estados del actuador
        router.get("/api/business/actuatorStates/:id_actuator/latest").handler(ctx -> {
            String idActuator = ctx.pathParam("id_actuator");

            // TODO: consultar últimos 10 estados en la tabla ActuatorState
            ctx.response().putHeader("Content-Type", "application/json");
            ctx.response().end("[ESTADOS_ACTUADOR_SIMULADOS]");
        });

        // 4. Último valor de cada sensor del grupo
        router.get("/api/business/group/:id_grupo/sensorValues/latest").handler(ctx -> {
            String idGrupo = ctx.pathParam("id_grupo");

            // TODO: consultar todos los sensores del grupo y devolver último valor de cada uno
            ctx.response().putHeader("Content-Type", "application/json");
            ctx.response().end("[ULTIMOS_VALORES_POR_SENSOR_DEL_GRUPO]");
        });

        // 5. Último estado de cada actuador del grupo
        router.get("/api/business/group/:id_grupo/actuatorStates/latest").handler(ctx -> {
            String idGrupo = ctx.pathParam("id_grupo");

            // TODO: consultar todos los actuadores del grupo y devolver último estado de cada uno
            ctx.response().putHeader("Content-Type", "application/json");
            ctx.response().end("[ULTIMOS_ESTADOS_POR_ACTUADOR_DEL_GRUPO]");
        });

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8090, http -> {
                if (http.succeeded()) {
                    System.out.println("BusinessRestVerticle escuchando en el puerto 8090");
                    startFuture.complete();
                } else {
                    startFuture.fail(http.cause());
                }
            });
    }
}
