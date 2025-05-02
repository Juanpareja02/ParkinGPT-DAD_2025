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
        this.gson = new Gson();
    }

    @Override
    public void start(Promise<Void> startFuture) {
        MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));

        mqttClient.connect(1883, "localhost", ar -> {
            if (ar.succeeded()) {
                System.out.println("Conectado al broker MQTT.");

                // Ejemplo: obtener lista de canales MQTT desde base de datos o lógica
                List<String> canales = controller.getMQTTChannels(); // <- Este método deberías implementarlo

                for (String canal : canales) {
                    mqttClient.subscribe(canal, MqttQoS.AT_LEAST_ONCE.value(), handler -> {
                        if (handler.succeeded()) {
                            System.out.println("Suscrito al canal MQTT: " + canal);
                        } else {
                            System.err.println("Error al suscribirse al canal: " + canal);
                        }
                    });
                }

                mqttClient.publishHandler(message -> {
                    System.out.println("Mensaje recibido en topic: " + message.topicName());
                    //System.out.println("Recibida petición para sensorData: " + idSensor + " -> " + valor);


                    try {
                        String json = message.payload().toString("UTF-8");
                        JsonObject data = gson.fromJson(json, JsonObject.class);

                        String sensorId = data.get("id_sensor").getAsString();
                        float valor = data.get("valor").getAsFloat();

                        // Se pasa el valor al controlador de lógica de negocio
                        boolean necesitaActivar = controller.evaluarSensor(sensorId, valor);

                        if (necesitaActivar) {
                            String actuatorId = controller.getActuatorIdForSensor(sensorId);
                            boolean nuevoEstado = controller.getNextActuatorState(actuatorId);

                            JsonObject mensajeMQTT = new JsonObject();
                            mensajeMQTT.addProperty("actuatorId", actuatorId);
                            mensajeMQTT.addProperty("state", nuevoEstado);

                            mqttClient.publish(
                                controller.getCanalActuador(actuatorId), // canal del actuador
                                Buffer.buffer(mensajeMQTT.toString()),
                                MqttQoS.AT_LEAST_ONCE,
                                false,
                                false
                            );
                            System.out.println("Publicado cambio de estado en actuador " + actuatorId + ": " + nuevoEstado);
                        }

                    } catch (JsonSyntaxException e) {
                        System.err.println("Error de sintaxis JSON en mensaje MQTT: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Error procesando el mensaje MQTT: " + e.getMessage());
                    }
                });

                startFuture.complete();
            } else {
                System.err.println("Error conectando al broker MQTT: " + ar.cause().getMessage());
                startFuture.fail(ar.cause());
            }
        });
    }
}
