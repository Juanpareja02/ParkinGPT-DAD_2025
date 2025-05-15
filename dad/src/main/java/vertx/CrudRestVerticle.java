package vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

public class CrudRestVerticle extends AbstractVerticle {

    private JDBCClient jdbc;

    @Override
    public void start(Promise<Void> startFuture) {

        jdbc = JDBCClient.createShared(vertx, new JsonObject()
        	.put("driver_class", "org.mariadb.jdbc.Driver")
        	.put("url", "jdbc:mariadb://localhost:3306/parkingpt_db")
            .put("user", "root")
            .put("password", "Gratis")
        );
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        createCrud(router, "users", new String[]{"nombre", "usuario", "contraseña", "matricula"});
        createCrud(router, "groups", new String[]{"nombre", "canal_mqtt"});
        createCrud(router, "devices", new String[]{"plaza", "id_grupo"});
        createCrud(router, "sensors", new String[]{"nombre", "tipo", "identificador", "id_dispositivo"});
        createCrud(router, "actuators", new String[]{"nombre", "tipo", "identificador", "id_dispositivo"});

        // sensor_values
        router.post("/api/sensorValues").handler(ctx -> {
            JsonObject body = ctx.getBodyAsJson();
            String sql = "INSERT INTO sensor_values (id_sensor, valor) VALUES (?, ?)";
            jdbc.updateWithParams(sql, new io.vertx.core.json.JsonArray()
                    .add(body.getInteger("id_sensor"))
                    .add(body.getFloat("valor")),
                res -> ctx.response().end("SensorValue insertado"));
        });

        router.get("/api/sensorValues/:id_sensor").handler(ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id_sensor"));
            jdbc.queryWithParams("SELECT * FROM sensor_values WHERE id_sensor = ?",
                new io.vertx.core.json.JsonArray().add(id),
                res -> ctx.response().putHeader("Content-Type", "application/json")
                        .end(res.result().getRows().toString()));
        });

        // actuator_states
        router.post("/api/actuatorStates").handler(ctx -> {
            JsonObject body = ctx.getBodyAsJson();
            String sql = "INSERT INTO actuator_states (id_actuator, estado) VALUES (?, ?)";
            jdbc.updateWithParams(sql, new io.vertx.core.json.JsonArray()
                    .add(body.getInteger("id_actuator"))
                    .add(body.getBoolean("estado")),
                res -> ctx.response().end("ActuatorState insertado"));
        });

        router.get("/api/actuatorStates/:id_actuator").handler(ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id_actuator"));
            jdbc.queryWithParams("SELECT * FROM actuator_states WHERE id_actuator = ?",
                new io.vertx.core.json.JsonArray().add(id),
                res -> ctx.response().putHeader("Content-Type", "application/json")
                        .end(res.result().getRows().toString()));
        });

        vertx.createHttpServer().requestHandler(router).listen(8088, result -> {
            if (result.succeeded()) {
                startFuture.complete();
                System.out.println("CRUD API escuchando en puerto 8088");
            } else {
                startFuture.fail(result.cause());
            }
        });
    }

    private void createCrud(Router router, String entity, String[] fields) {
        String base = "/api/" + entity;

        router.post(base).handler(ctx -> {
            JsonObject body = ctx.getBodyAsJson();
            StringBuilder sql = new StringBuilder("INSERT INTO " + entity + " (");
            StringBuilder values = new StringBuilder(" VALUES (");
            io.vertx.core.json.JsonArray params = new io.vertx.core.json.JsonArray();
            for (int i = 0; i < fields.length; i++) {
                sql.append(fields[i]);
                values.append("?");
                params.add(body.getValue(fields[i]));
                if (i < fields.length - 1) {
                    sql.append(", ");
                    values.append(", ");
                }
            }
            sql.append(")").append(values).append(")");
            jdbc.updateWithParams(sql.toString(), params, res -> {
                if (res.succeeded()) ctx.response().end(entity + " insertado");
                else ctx.response().setStatusCode(500).end(res.cause().getMessage());
            });
        });

        router.get(base).handler(ctx -> {
            jdbc.query("SELECT * FROM " + entity, res -> {
                if (res.succeeded())
                    ctx.response().putHeader("Content-Type", "application/json")
                        .end(res.result().getRows().toString());
                else ctx.response().setStatusCode(500).end(res.cause().getMessage());
            });
        });

        router.get(base + "/:id").handler(ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            jdbc.queryWithParams("SELECT * FROM " + entity + " WHERE id = ?",
                new io.vertx.core.json.JsonArray().add(id),
                res -> ctx.response().putHeader("Content-Type", "application/json")
                        .end(res.result().getRows().toString()));
        });

        router.put(base + "/:id").handler(ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            JsonObject body = ctx.getBodyAsJson();
            StringBuilder sql = new StringBuilder("UPDATE " + entity + " SET ");
            io.vertx.core.json.JsonArray params = new io.vertx.core.json.JsonArray();
            for (int i = 0; i < fields.length; i++) {
                sql.append(fields[i]).append(" = ?");
                params.add(body.getValue(fields[i]));
                if (i < fields.length - 1) sql.append(", ");
            }
            sql.append(" WHERE id = ?");
            params.add(id);
            jdbc.updateWithParams(sql.toString(), params, res -> {
                if (res.succeeded()) ctx.response().end(entity + " actualizado");
                else ctx.response().setStatusCode(500).end(res.cause().getMessage());
            });
        });

        router.delete(base + "/:id").handler(ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            jdbc.updateWithParams("DELETE FROM " + entity + " WHERE id = ?",
                new io.vertx.core.json.JsonArray().add(id),
                res -> {
                    if (res.succeeded()) ctx.response().end(entity + " eliminado");
                    else ctx.response().setStatusCode(500).end(res.cause().getMessage());
                });
        });
    }
}
