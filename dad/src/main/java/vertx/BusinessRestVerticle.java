/*  BusinessRestVerticle.java  ▸  API de alto nivel
 *  -------------------------------------------------
 *  - Solo expone endpoints “de negocio”.
 *  - Nunca toca la BD: toda operación se delega a la API CRUD (localhost:8088).
 *  - Decide lógica de rango y publica eventos en el EventBus.
 */

package vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

public class BusinessRestVerticle extends AbstractVerticle {

  private static final String CRUD_HOST = "localhost";
  private static final int    CRUD_PORT = 8088;

  private WebClient client;

  @Override
  public void start(Promise<Void> startFuture) {

    client = WebClient.create(vertx);                 // HTTP client → API CRUD

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    /* -------------------------------------------------------
     * 1) POST /api/business/sensorData
     *    1.1  llama a CRUD  →  /api/sensorValues     (insert)
     *    1.2  llama a CRUD  ←  /api/sensor_ranges/{id}
     *    1.3  publica sensor.inRange | sensor.outOfRange
     * -----------------------------------------------------*/
    router.post("/api/business/sensorData").handler(ctx -> {

      JsonObject body = ctx.getBodyAsJson();
      if (body == null || !body.containsKey("id_sensor") || !body.containsKey("valor")) {
        ctx.response().setStatusCode(400).end("JSON {id_sensor, valor} requerido");
        return;
      }

      int   idSensor = body.getInteger("id_sensor");
      float valor    = body.getFloat("valor");

      /* 1.1 INSERT sensor_values vía CRUD */
      client.post(CRUD_PORT, CRUD_HOST, "/api/sensorValues")
            .sendJsonObject(new JsonObject()
                .put("id_sensor", idSensor)
                .put("valor",     valor), arInsert -> {

        if (arInsert.failed()) {
          ctx.response().setStatusCode(502).end("CRUD no disponible");
          return;
        }

        /* 1.2 GET rango vía CRUD */
        client.get(CRUD_PORT, CRUD_HOST, "/api/sensor_ranges/" + idSensor)
              .as(BodyCodec.jsonArray())
              .send(arRange -> {

          if (arRange.failed() || arRange.result().body().isEmpty()) {
            ctx.response().setStatusCode(404).end("Rango no encontrado");
            return;
          }

          JsonObject range = arRange.result().body().getJsonObject(0);
          float min = range.getFloat("min_value");
          float max = range.getFloat("max_value");

          JsonObject evt = new JsonObject()
              .put("id_sensor", idSensor)
              .put("valor",     valor);

          /* 1.3 Lógica de negocio → EventBus */
          if (valor < min || valor > max)
               vertx.eventBus().publish("sensor.outOfRange", evt);
          else vertx.eventBus().publish("sensor.inRange",    evt);

          ctx.response().end("SensorData procesado (Business API)");
        });
      });
    });

    /* 2) GET últimos 10 valores de sensor */
    router.get("/api/business/sensorValues/:id/latest").handler(ctx -> {
      int id = Integer.parseInt(ctx.pathParam("id"));
      client.get(CRUD_PORT, CRUD_HOST,
                 "/api/sensorValues/" + id)          // llama directamente al CRUD
            .as(BodyCodec.string())
            .send(ar -> {
              if (ar.succeeded())
                   ctx.response().putHeader("Content-Type","application/json")
                                 .end(ar.result().body());
              else ctx.response().setStatusCode(502).end("CRUD no disponible");
            });
    });

    /* 3) GET últimos 10 estados de actuador */
    router.get("/api/business/actuatorStates/:id/latest").handler(ctx -> {
      int id = Integer.parseInt(ctx.pathParam("id"));
      client.get(CRUD_PORT, CRUD_HOST,
                 "/api/actuatorStates/" + id)
            .as(BodyCodec.string())
            .send(ar -> {
              if (ar.succeeded())
                   ctx.response().putHeader("Content-Type","application/json")
                                 .end(ar.result().body());
              else ctx.response().setStatusCode(502).end("CRUD no disponible");
            });
    });

    /* ---------- servidor HTTP ---------- */
    vertx.createHttpServer()
         .requestHandler(router)
         .listen(8090, ar -> {
           if (ar.succeeded()) {
             System.out.println("BusinessRestVerticle listening on 8090");
             startFuture.complete();
           } else startFuture.fail(ar.cause());
         });
  }
}
