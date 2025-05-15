package vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class BusinessRestVerticle extends AbstractVerticle {

    private JDBCClient jdbc;

    @Override
    public void start(Promise<Void> startFuture) {
        // Configurar JDBC (MariaDB)
        jdbc = JDBCClient.createShared(vertx, new JsonObject()
            .put("url", "jdbc:mariadb://localhost:3306/parkingpt_db?useSSL=false&serverTimezone=UTC")
            .put("driver_class", "org.mariadb.jdbc.Driver")
            .put("user", "root")
            .put("password", "Gratis")
        );

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // 1) Recibir datos del sensor y publicar evento según rango
        router.post("/api/business/sensorData").handler(ctx -> {
            JsonObject body = ctx.getBodyAsJson();
            int idSensor = body.getInteger("id_sensor");
            float valor = body.getFloat("valor");

            // 1. Insertar en sensor_values
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
                    // 2. Consultar rango
                    String rangeSQL = "SELECT min_value, max_value FROM sensor_ranges WHERE id_sensor = ?";
                    conn.queryWithParams(rangeSQL, new JsonArray().add(idSensor), qr -> {
                        if (qr.succeeded() && !qr.result().getRows().isEmpty()) {
                            JsonObject row = qr.result().getRows().get(0);
                            float min = row.getFloat("min_value");
                            float max = row.getFloat("max_value");

                            JsonObject evt = new JsonObject()
                                .put("id_sensor", idSensor)
                                .put("valor", valor);

                            // Publicar en EventBus según dentro o fuera de rango
                            if (valor < min || valor > max) {
                                vertx.eventBus().publish("sensor.outOfRange", evt);
                            } else {
                                vertx.eventBus().publish("sensor.inRange", evt);
                            }
                        }
                        ctx.response().setStatusCode(200).end("SensorData processed");
                        conn.close();
                    });
                });
            });
        });


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
