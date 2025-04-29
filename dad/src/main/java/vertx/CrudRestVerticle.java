package vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.ResultSet;

public class CrudRestVerticle extends AbstractVerticle {

    private JDBCClient jdbc;

    @Override
    public void start(Promise<Void> startFuture) {

        jdbc = JDBCClient.createShared(vertx, new JsonObject()
            .put("url", "jdbc:mysql://localhost:3306/parkingpt_db")
            .put("driver_class", "com.mysql.cj.jdbc.Driver")
            .put("user", "tu_usuario_mysql")
            .put("password", "tu_contraseña_mysql")
        );

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // POST /api/sensors
        router.post("/api/sensors").handler(ctx -> {
            JsonObject body = ctx.getBodyAsJson();
            String sql = "INSERT INTO sensors (nombre, tipo, identificador, id_dispositivo) VALUES (?, ?, ?, ?)";
            jdbc.getConnection(conn -> {
                if (conn.succeeded()) {
                    SQLConnection connection = conn.result();
                    connection.updateWithParams(sql, 
                        new io.vertx.core.json.JsonArray()
                            .add(body.getString("nombre"))
                            .add(body.getString("tipo"))
                            .add(body.getString("identificador"))
                            .add(body.getInteger("id_dispositivo")), 
                        res -> {
                            if (res.succeeded()) {
                                ctx.response().setStatusCode(201).end("Sensor insertado");
                            } else {
                                ctx.response().setStatusCode(500).end(res.cause().getMessage());
                            }
                            connection.close();
                        });
                } else {
                    ctx.response().setStatusCode(500).end("Error de conexión DB");
                }
            });
        });

        // GET /api/sensors
        router.get("/api/sensors").handler(ctx -> {
            jdbc.query("SELECT * FROM sensors", res -> {
                if (res.succeeded()) {
                    ctx.response().putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("sensors", res.result().getRows()).encode());
                } else {
                    ctx.response().setStatusCode(500).end(res.cause().getMessage());
                }
            });
        });

        // GET /api/sensors/:id
        router.get("/api/sensors/:id").handler(ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            jdbc.queryWithParams("SELECT * FROM sensors WHERE id = ?", 
                new io.vertx.core.json.JsonArray().add(id), res -> {
                    if (res.succeeded()) {
                        ctx.response().putHeader("Content-Type", "application/json")
                            .end(res.result().getRows().get(0).encode());
                    } else {
                        ctx.response().setStatusCode(500).end(res.cause().getMessage());
                    }
                });
        });

        // PUT /api/sensors/:id
        router.put("/api/sensors/:id").handler(ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            JsonObject body = ctx.getBodyAsJson();
            String sql = "UPDATE sensors SET nombre = ?, tipo = ?, identificador = ?, id_dispositivo = ? WHERE id = ?";
            jdbc.updateWithParams(sql,
                new io.vertx.core.json.JsonArray()
                    .add(body.getString("nombre"))
                    .add(body.getString("tipo"))
                    .add(body.getString("identificador"))
                    .add(body.getInteger("id_dispositivo"))
                    .add(id),
                res -> {
                    if (res.succeeded()) {
                        ctx.response().end("Sensor actualizado");
                    } else {
                        ctx.response().setStatusCode(500).end(res.cause().getMessage());
                    }
                });
        });

        // DELETE /api/sensors/:id
        router.delete("/api/sensors/:id").handler(ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            jdbc.updateWithParams("DELETE FROM sensors WHERE id = ?", 
                new io.vertx.core.json.JsonArray().add(id),
                res -> {
                    if (res.succeeded()) {
                        ctx.response().end("Sensor eliminado");
                    } else {
                        ctx.response().setStatusCode(500).end(res.cause().getMessage());
                    }
                });
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
}
