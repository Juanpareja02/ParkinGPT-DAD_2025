package vertx;

import com.google.gson.Gson;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class BusinessRestVerticle extends AbstractVerticle {

    private final Gson gson = new Gson();
    private JDBCClient jdbc;

    @Override
    public void start(Promise<Void> startFuture) {
        // Configurar conexión JDBC con MariaDB
        jdbc = JDBCClient.createShared(vertx, new JsonObject()
            .put("url", "jdbc:mariadb://localhost:3306/parkingpt_db?useSSL=false&serverTimezone=UTC")
            .put("driver_class", "org.mariadb.jdbc.Driver")
            .put("user", "root")
            .put("password", "Gratis")
        );

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // 1. Recibir datos del sensor y procesar lógica
        router.post("/api/business/sensorData").handler(ctx -> {
            JsonObject body = ctx.getBodyAsJson();
            int idSensor = body.getInteger("id_sensor");
            float valor = body.getFloat("valor");

            jdbc.getConnection(connRes -> {
                if (connRes.failed()) {
                    ctx.response().setStatusCode(500).end("DB connection error");
                    return;
                }
                SQLConnection conn = connRes.result();
                String insertSQL = "INSERT INTO sensor_values (id_sensor, valor) VALUES (?, ?)";
                conn.updateWithParams(insertSQL, new JsonArray().add(idSensor).add(valor), upRes -> {
                    if (upRes.failed()) {
                        ctx.response().setStatusCode(500).end("Error inserting sensor value");
                        conn.close();
                        return;
                    }
                    // Consultar rango
                    String rangeSQL = "SELECT min_value, max_value FROM sensor_ranges WHERE id_sensor = ?";
                    conn.queryWithParams(rangeSQL, new JsonArray().add(idSensor), qr -> {
                        if (qr.succeeded() && !qr.result().getRows().isEmpty()) {
                            JsonObject row = qr.result().getRows().get(0);
                            float min = row.getFloat("min_value");
                            float max = row.getFloat("max_value");
                            boolean fuera = valor < min || valor > max;
                            if (fuera) {
                                // Publicar evento de negocio (puede suscribirse MQTT)
                                JsonObject evt = new JsonObject()
                                    .put("id_sensor", idSensor)
                                    .put("valor", valor);
                                vertx.eventBus().publish("sensor.outOfRange", evt);
                            }
                        }
                        ctx.response().setStatusCode(200).end("SensorData processed");
                        conn.close();
                    });
                });
            });
        });

     // 2. Últimos 10 valores del sensor
        router.get("/api/business/sensorValues/:id_sensor/latest").handler(ctx -> {
            int idSensor = Integer.parseInt(ctx.pathParam("id_sensor"));
            String sql = ""
              + "SELECT id_sensor, valor, "
              + "DATE_FORMAT(created_at, '%Y-%m-%dT%H:%i:%s') AS created_at "
              + "FROM sensor_values "
              + "WHERE id_sensor = ? "
              + "ORDER BY created_at DESC "
              + "LIMIT 10";
            jdbc.queryWithParams(sql, new JsonArray().add(idSensor), res -> {
                if (res.succeeded()) {
                    ctx.response()
                       .putHeader("Content-Type", "application/json")
                       .end(res.result().getRows().toString());
                } else {
                    ctx.response().setStatusCode(500)
                       .end("Error querying sensor values: " + res.cause().getMessage());
                }
            });
        });

        // 3. Últimos 10 estados del actuador
        router.get("/api/business/actuatorStates/:id_actuator/latest").handler(ctx -> {
            int idAct = Integer.parseInt(ctx.pathParam("id_actuator"));
            String sql = ""
              + "SELECT id_actuator, estado, "
              + "DATE_FORMAT(created_at, '%Y-%m-%dT%H:%i:%s') AS created_at "
              + "FROM actuator_states "
              + "WHERE id_actuator = ? "
              + "ORDER BY created_at DESC "
              + "LIMIT 10";
            jdbc.queryWithParams(sql, new JsonArray().add(idAct), res -> {
                if (res.succeeded()) {
                    ctx.response()
                       .putHeader("Content-Type", "application/json")
                       .end(res.result().getRows().toString());
                } else {
                    ctx.response().setStatusCode(500)
                       .end("Error querying actuator states: " + res.cause().getMessage());
                }
            });
        });

        // 4. Último valor de cada sensor del grupo
        router.get("/api/business/group/:id_grupo/sensorValues/latest").handler(ctx -> {
            int idGrupo = Integer.parseInt(ctx.pathParam("id_grupo"));
            String sql = ""
              + "SELECT s.id AS sensor_id, sv.valor, "
              + "DATE_FORMAT(sv.created_at, '%Y-%m-%dT%H:%i:%s') AS created_at "
              + "FROM sensors s "
              + "JOIN devices d ON s.id_dispositivo = d.id "
              + "JOIN sensor_values sv ON s.id = sv.id_sensor "
              + "WHERE d.id_grupo = ? "
              + "AND sv.created_at = ("
              + "  SELECT MAX(created_at) "
              + "  FROM sensor_values "
              + "  WHERE id_sensor = s.id"
              + ")"; 
            jdbc.queryWithParams(sql, new JsonArray().add(idGrupo), res -> {
                if (res.succeeded()) {
                    ctx.response()
                       .putHeader("Content-Type", "application/json")
                       .end(res.result().getRows().toString());
                } else {
                    ctx.response().setStatusCode(500)
                       .end("Error querying group sensor values: " + res.cause().getMessage());
                }
            });
        });

        // 5. Último estado de cada actuador del grupo
        router.get("/api/business/group/:id_grupo/actuatorStates/latest").handler(ctx -> {
            int idGrupo = Integer.parseInt(ctx.pathParam("id_grupo"));
            String sql = ""
              + "SELECT a.id AS actuator_id, ast.estado, "
              + "DATE_FORMAT(ast.created_at, '%Y-%m-%dT%H:%i:%s') AS created_at "
              + "FROM actuators a "
              + "JOIN devices d ON a.id_dispositivo = d.id "
              + "JOIN actuator_states ast ON a.id = ast.id_actuator "
              + "WHERE d.id_grupo = ? "
              + "AND ast.created_at = ("
              + "  SELECT MAX(created_at) "
              + "  FROM actuator_states "
              + "  WHERE id_actuator = a.id"
              + ")"; 
            jdbc.queryWithParams(sql, new JsonArray().add(idGrupo), res -> {
                if (res.succeeded()) {
                    ctx.response()
                       .putHeader("Content-Type", "application/json")
                       .end(res.result().getRows().toString());
                } else {
                    ctx.response().setStatusCode(500)
                       .end("Error querying group actuator states: " + res.cause().getMessage());
                }
            });
        });

        // Iniciar servidor HTTP en el puerto 8090
        vertx.createHttpServer()
             .requestHandler(router)
             .listen(8090, http -> {
                 if (http.succeeded()) {
                     System.out.println("BusinessRestVerticle listening on port 8090");
                     startFuture.complete();
                 } else {
                     startFuture.fail(http.cause());
                 }
             });
    }
}
