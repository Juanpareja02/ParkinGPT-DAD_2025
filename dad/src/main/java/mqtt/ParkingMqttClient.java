package mqtt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import parkingpt.ParkingController;

import java.util.List;

public class ParkingMqttClient extends AbstractVerticle {

private Gson gson;
private ParkingController controller;

public ParkingMqttClient(ParkingController controller) {
    this.controller = controller;
}

@Override
public void start(Promise<Void> startFuture) {
    gson = new Gson();
    MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));

    mqttClient.connect(1883, "localhost", s -> {
        if (s.succeeded()) {
            System.out.println("Conectado al broker MQTT.");

            List<String> canales = controller.getMQTTChannels();
            for (String canal : canales) {
                mqttClient.subscribe(canal, MqttQoS.AT_LEAST_ONCE.value(), handler -> {
                    if (handler.succeeded()) {
                        System.out.println("Suscripción al canal: " + canal);
                    } else {
                        System.err.println("Error al suscribirse al canal: " + canal);
                    }
                });
            }

            mqttClient.publishHandler(handler -> {
                System.out.println("Mensaje recibido:");
                System.out.println("    Topic: " + handler.topicName());
                System.out.println("    Id del mensaje: " + handler.messageId());
                System.out.println("    Contenido: " + handler.payload().toString());

                try {
                    String json = handler.payload().toString("UTF-8");
                    JsonObject data = gson.fromJson(json, JsonObject.class);

                    String sensorId = data.get("id_sensor").getAsString();
                    float valor = data.get("valor").getAsFloat();

                    boolean necesitaActivar = controller.evaluarSensor(sensorId, valor);

                    if (necesitaActivar) {
                        String actuatorId = controller.getActuatorIdForSensor(sensorId);
                        boolean nuevoEstado = controller.getNextActuatorState(actuatorId);

                        JsonObject mensajeMQTT = new JsonObject();
                        mensajeMQTT.addProperty("actuatorId", actuatorId);
                        mensajeMQTT.addProperty("state", nuevoEstado);

                        mqttClient.publish(
                            controller.getCanalActuador(actuatorId),
                            Buffer.buffer(mensajeMQTT.toString()),
                            MqttQoS.AT_LEAST_ONCE,
                            false,
                            false
                        );
                        System.out.println("Publicado cambio de estado en actuador " + actuatorId + ": " + nuevoEstado);
                    }
                } catch (JsonSyntaxException e) {
                    System.err.println("    Error de sintaxis JSON: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("    Error procesando mensaje: " + e.getMessage());
                }
            });

            // Publicación inicial de prueba
            mqttClient.publish("test/connection", Buffer.buffer("MQTT listo"), MqttQoS.AT_LEAST_ONCE, false, false);

            startFuture.complete();
        } else {
            System.err.println("Error conectando al broker MQTT: " + s.cause().getMessage());
            startFuture.fail(s.cause());
        }
    });
}

}
