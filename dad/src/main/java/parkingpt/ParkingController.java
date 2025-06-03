package parkingpt;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Future;
import mqtt.ParkingMqttClient;
import vertx.BusinessRestVerticle;
import vertx.CrudRestVerticle;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

import java.util.*;

public class ParkingController extends AbstractVerticle {

    // Rangos configurados por sensor (idSensor → [min, max])
    private final Map<Integer, Float[]> rangosPermitidos = new HashMap<>();
    // Asociaciones sensor → actuador (rojo/out)
    private final Map<Integer, String> sensorToActuatorOut = new HashMap<>();
    // Asociaciones sensor → actuador (verde/in)
    private final Map<Integer, String> sensorToActuatorIn = new HashMap<>();
    // Estado actual de cada actuador (idActuator → estado)
    private final Map<String, Boolean> actuadorEstado = new HashMap<>();
    // Canal MQTT por actuador (idActuator → topic)
    private final Map<String, String> actuadorCanal = new HashMap<>();
    // Canales MQTT de sensores a los que suscribirse
    private final List<String> canalesMQTT = new ArrayList<>();

    private static final String CRUD_HOST = "localhost";
    private static final int    CRUD_PORT = 8088;

    private WebClient client;

    @Override
    public void start(Promise<Void> startFuture) {
        client = WebClient.create(vertx);

        cargarConfiguracion().onComplete(ar -> {
            if (ar.succeeded()) {
                vertx.deployVerticle(new ParkingMqttClient(this));
                vertx.deployVerticle(CrudRestVerticle.class.getName());
                vertx.deployVerticle(BusinessRestVerticle.class.getName());

                System.out.println("ParkingController desplegó todos los verticles correctamente.");
                startFuture.complete();
            } else {
                startFuture.fail(ar.cause());
            }
        });
    }

    private Future<Void> cargarConfiguracion() {
        Promise<Void> promise = Promise.promise();
        client.get(CRUD_PORT, CRUD_HOST, "/api/sensor_ranges")
              .as(BodyCodec.jsonArray())
              .send(ar -> {
                  if (ar.failed()) {
                      promise.fail(ar.cause());
                      return;
                  }

                  ar.result().body().forEach(obj -> {
                      io.vertx.core.json.JsonObject json = (io.vertx.core.json.JsonObject) obj;
                      int idSensor = json.getInteger("id_sensor");
                      float min = json.getFloat("min_value");
                      float max = json.getFloat("max_value");
                      rangosPermitidos.put(idSensor, new Float[]{min, max});
                  });

                  client.get(CRUD_PORT, CRUD_HOST, "/api/actuators")
                        .as(BodyCodec.jsonArray())
                        .send(arAct -> {
                            if (arAct.failed()) {
                                promise.fail(arAct.cause());
                                return;
                            }

                            io.vertx.core.json.JsonArray acts = arAct.result().body();

                            for (int i = 0; i < acts.size(); i++) {
                                io.vertx.core.json.JsonObject act = acts.getJsonObject(i);
                                String ident = act.getString("identificador");
                                String nombre = act.getString("nombre", "").toLowerCase();
                                actuadorEstado.put(ident, false);
                                if (nombre.contains("rojo")) {
                                    sensorToActuatorOut.putIfAbsent(1, ident);
                                } else if (nombre.contains("verde")) {
                                    sensorToActuatorIn.putIfAbsent(1, ident);
                                }
                            }

                            client.get(CRUD_PORT, CRUD_HOST, "/api/groups")
                                  .as(BodyCodec.jsonArray())
                                  .send(arGrp -> {
                                      if (arGrp.failed()) {
                                          promise.fail(arGrp.cause());
                                          return;
                                      }

                                      arGrp.result().body().forEach(o -> {
                                          io.vertx.core.json.JsonObject g = (io.vertx.core.json.JsonObject) o;
                                          String canal = g.getString("canal_mqtt");
                                          canalesMQTT.add(canal);
                                          // Genera tópicos para actuadores basados en el canal del grupo
                                          String rojo = canal.replace("sensor", "actuador_rojo");
                                          String verde = canal.replace("sensor", "actuador_verde");
                                          sensorToActuatorOut.values().forEach(id -> actuadorCanal.put(id, rojo));
                                          sensorToActuatorIn.values().forEach(id -> actuadorCanal.put(id, verde));
                                      });

                                      promise.complete();
                                  });
                        });
              });
        return promise.future();
    }

    /** Evalúa si el valor del sensor está fuera de rango */
    public boolean evaluarSensor(int idSensor, float valor) {
        Float[] rango = rangosPermitidos.getOrDefault(idSensor, new Float[]{0f, 100f});
        return valor < rango[0] || valor > rango[1];
    }

    /** Actuador rojo (out) para un sensor dado */
    public String getActuatorOutForSensor(int sensorId) {
        return sensorToActuatorOut.get(sensorId);
    }

    /** Actuador verde (in) para un sensor dado */
    public String getActuatorInForSensor(int sensorId) {
        return sensorToActuatorIn.get(sensorId);
    }

    /** Alterna y devuelve el nuevo estado de un actuador */
    public boolean getNextActuatorState(String actuatorId) {
        boolean current = actuadorEstado.getOrDefault(actuatorId, false);
        boolean next = !current;
        actuadorEstado.put(actuatorId, next);
        return next;
    }

    /** Topic MQTT asociado a un actuador */
    public String getCanalActuador(String actuatorId) {
        return actuadorCanal.getOrDefault(actuatorId, "canal_actuador_default");
    }

    /** Lista de topics de sensores para suscribirse */
    public List<String> getMQTTChannels() {
        return canalesMQTT;
    }


}
