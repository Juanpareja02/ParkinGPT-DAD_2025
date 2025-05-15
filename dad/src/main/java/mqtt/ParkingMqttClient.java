package mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import parkingpt.ParkingController;

import java.util.List;

public class ParkingMqttClient extends AbstractVerticle {

    private final ParkingController controller;
    private MqttClient mqttClient;

    public ParkingMqttClient(ParkingController controller) {
        this.controller = controller;
    }

    @Override
    public void start(Promise<Void> startFuture) {
        mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));

        mqttClient.connect(1883, "localhost", ar -> {
            if (ar.succeeded()) {
                System.out.println("Conectado al broker MQTT.");

                // Suscripción al canal de sensores
                List<String> canales = controller.getMQTTChannels();
                canales.forEach(canal -> mqttClient.subscribe(canal, MqttQoS.AT_LEAST_ONCE.value(), sub -> {
                    if (sub.succeeded()) {
                        System.out.println("Suscrito al canal: " + canal);
                    } else {
                        System.err.println("Error suscripción: " + sub.cause().getMessage());
                    }
                }));

                // Consumer para OUT OF RANGE → actuador rojo
                MessageConsumer<JsonObject> outConsumer = vertx.eventBus().consumer("sensor.outOfRange");
                outConsumer.handler(msg -> {
                    JsonObject body = msg.body();
                    int sensorId = body.getInteger("id_sensor");
                    Double valor = (Double) body.getDouble("valor");

                    String actuatorId = controller.getActuatorOutForSensor(sensorId);
                    boolean newState = controller.getNextActuatorState(actuatorId);

                    JsonObject out = new JsonObject()
                        .put("actuatorId", actuatorId)
                        .put("state", newState);

                    String topic = controller.getCanalActuador(actuatorId);
                    mqttClient.publish(topic, Buffer.buffer(out.encode()), MqttQoS.AT_LEAST_ONCE, false, false);
                    System.out.println("Rojo publicado en " + topic + ": " + out.encode());
                });

                // Consumer para IN RANGE → actuador verde
                MessageConsumer<JsonObject> inConsumer = vertx.eventBus().consumer("sensor.inRange");
                inConsumer.handler(msg -> {
                    JsonObject body = msg.body();
                    int sensorId = body.getInteger("id_sensor");
                    Double valor = (Double) body.getDouble("valor");

                    String actuatorId = controller.getActuatorInForSensor(sensorId);
                    // Enciende siempre en rango (verde)
                    boolean newState = true;
                    controller.getNextActuatorState(actuatorId); 

                    JsonObject out = new JsonObject()
                        .put("actuatorId", actuatorId)
                        .put("state", newState);

                    String topic = controller.getCanalActuador(actuatorId);
                    mqttClient.publish(topic, Buffer.buffer(out.encode()), MqttQoS.AT_LEAST_ONCE, false, false);
                    System.out.println("Verde publicado en " + topic + ": " + out.encode());
                });

                startFuture.complete();
            } else {
                System.err.println("Error conectando al broker MQTT: " + ar.cause().getMessage());
                startFuture.fail(ar.cause());
            }
        });
    }
}
