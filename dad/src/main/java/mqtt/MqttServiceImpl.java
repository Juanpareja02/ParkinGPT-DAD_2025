package mqtt;

import java.util.function.Consumer;

import com.google.gson.JsonObject;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

public class MqttServiceImpl {
	 private static final Logger LOGGER = LoggerFactory.getLogger(MqttServiceImpl.class);

	    private final Vertx vertx;
	    private final MqttClientOptions options;
	    private MqttClient client;
	    private Consumer<JsonObject> messageHandler = null; // Handler para mensajes entrantes
	    private volatile boolean connected = false;


	    public MqttServiceImpl(Vertx vertx, JsonObject config) {
	        this.vertx = vertx;

	        // Configurar opciones del cliente MQTT desde config.json
	        options = new MqttClientOptions()
	            .setClientId(config.getString("mqtt.clientId", "ParkinGPT_Server_" + System.currentTimeMillis()))
	            .setUsername(config.getString("mqtt.username")) // Puede ser null si no hay auth
	            .setPassword(config.getString("mqtt.password")) // Puede ser null si no hay auth
	            .setAutoKeepAlive(true)
	            .setCleanSession(true) // Iniciar sesión limpia cada vez
	            .setReconnectAttempts(5) // Intentar reconectar 5 veces
	            .setReconnectInterval(5000); // Esperar 5 segundos entre intentos

	        String host = config.getString("mqtt.host", "localhost");
	        int port = config.getInteger("mqtt.port", 1883);

	        this.client = MqttClient.create(vertx, options);

	        // Manejadores básicos del cliente
	        client.closeHandler(v -> {
	            connected = false;
	            LOGGER.warn("MQTT client connection closed. Will attempt to reconnect if configured.");
	            // Vert.x MQTT client maneja la reconexión automáticamente si setReconnectAttempts > 0
	        });

	        client.exceptionHandler(e -> {
	            LOGGER.error("MQTT client exception: ", e);
	        });

	        // Manejador para mensajes entrantes (si nos suscribimos a algo)
	        client.publishHandler(message -> {
	            LOGGER.info("Received MQTT message: Topic='{}', QoS='{}', Payload='{}'",
	                message.topicName(), message.qosLevel(), message.payload().toString());
	            if (messageHandler != null) {
	                try {
	                    JsonObject payloadJson = message.payload().toJsonObject();
	                    messageHandler.accept(payloadJson);
	                } catch (Exception e) {
	                    LOGGER.error("Error processing incoming MQTT message payload as JSON: {}", message.payload().toString(), e);
	                }
	            }
	        });

	        LOGGER.info("MQTT Service initialized for broker at {}:{}", host, port);
	    }

	    @Override
	    public Future<Void> connect() {
	        if (connected) {
	            return Future.succeededFuture();
	        }
	        Promise<Void> promise = Promise.promise();
	        String host = options.getHost(); // Tomar de options por si se setean allí
	        int port = options.getPort();    // Tomar de options por si se setean allí

	        LOGGER.info("Attempting to connect to MQTT broker at {}:{} with client ID '{}'", host, port, options.getClientId());

	        client.connect(port, host, ar -> {
	            if (ar.succeeded()) {
	                connected = true;
	                LOGGER.info("Successfully connected to MQTT broker {}:{}", host, port);
	                promise.complete();
	            } else {
	                connected = false;
	                LOGGER.error("Failed to connect to MQTT broker {}:{}. Reason: {}", host, port, ar.cause().getMessage());
	                promise.fail(ar.cause());
	            }
	        });
	        return promise.future();
	    }

	    @Override
	    public Future<Void> disconnect() {
	         if (!connected) {
	            return Future.succeededFuture();
	        }
	        Promise<Void> promise = Promise.promise();
	        client.disconnect(ar -> {
	             connected = false;
	            if (ar.succeeded()) {
	                LOGGER.info("Successfully disconnected from MQTT broker.");
	                promise.complete();
	            } else {
	                 LOGGER.error("Failed to disconnect cleanly from MQTT broker.", ar.cause());
	                // Aún así, podríamos considerar completar, ya que la intención era desconectar
	                 promise.complete(); // O fail(ar.cause()) dependiendo de la semántica deseada
	            }
	        });
	        return promise.future();
	    }

	     @Override
	    public Future<Integer> publishActuatorCommand(String topic, String actuatorIdentifier, boolean state) {
	         if (!connected) {
	             String errorMsg = "MQTT client not connected. Cannot publish message.";
	             LOGGER.error(errorMsg);
	             return Future.failedFuture(new IllegalStateException(errorMsg));
	         }
	        Promise<Integer> promise = Promise.promise();

	        // Crear payload JSON según el formato especificado
	        JsonObject payload = new JsonObject()
	            .put("actuatorId", actuatorIdentifier)
	            .put("state", state);

	        Buffer buffer = payload.toBuffer();

	        LOGGER.debug("Publishing to MQTT: Topic='{}', Payload='{}'", topic, payload.encode());

	        // Publicar con QoS 1 (At Least Once) y sin retener el mensaje
	        client.publish(topic, buffer, MqttQoS.AT_LEAST_ONCE, false, false, ar -> {
	            if (ar.succeeded()) {
	                LOGGER.info("Successfully published message ID {} to topic '{}'", ar.result(), topic);
	                promise.complete(ar.result()); // ar.result() es el messageId
	            } else {
	                LOGGER.error("Failed to publish message to topic '{}'", topic, ar.cause());
	                promise.fail(ar.cause());
	            }
	        });
	        return promise.future();
	    }

	     public Future<Integer> subscribe(String topic, int qos) {
	         if (!connected) {
	             String errorMsg = "MQTT client not connected. Cannot subscribe to topic.";
	             LOGGER.error(errorMsg);
	             return Future.failedFuture(new IllegalStateException(errorMsg));
	         }
	        Promise<Integer> promise = Promise.promise();
	        MqttQoS mqttQos = MqttQoS.valueOf(qos); // Asegura QoS válido (0, 1, 2)

	        LOGGER.info("Subscribing to MQTT topic: '{}' with QoS {}", topic, qos);
	        client.subscribe(topic, mqttQos.value(), ar -> {
	            if (ar.succeeded()) {
	                LOGGER.info("Successfully subscribed to topic '{}' with result code {}", topic, ar.result());
	                promise.complete(ar.result()); // ar.result() es el packet ID, o a veces códigos de QoS concedidos
	            } else {
	                LOGGER.error("Failed to subscribe to topic '{}'", topic, ar.cause());
	                promise.fail(ar.cause());
	            }
	        });
	        return promise.future();
	    }

	    public void setMessageHandler(Consumer<JsonObject> handler) {
	        this.messageHandler = handler;
	    }

	    @Override
	    public boolean isConnected() {
	        // Asegurarse de que el estado refleje la conexión real si es posible,
	        // aunque el cliente Vert.x puede no tener un método isConnected directo síncrono fiable.
	        // Usar nuestro flag `connected` actualizado en los handlers es una aproximación.
	        return connected && client != null; // Añadir chequeo de null por si acaso
	    }
	}
