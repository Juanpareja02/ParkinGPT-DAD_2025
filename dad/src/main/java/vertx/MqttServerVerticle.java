package vertx;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import mqtt.MqttService;

public class MqttServerVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttClientVerticle.class);

    private final MqttService mqttService;
    private final DatabaseService dbService;

    public MqttClientVerticle(MqttService mqttService, DatabaseService dbService) {
        this.mqttService = mqttService;
        this.dbService = dbService;
    }

    public void start(Promise<Void> startPromise) {
        LOGGER.info("Starting MQTT Client Verticle...");

        // Conectar y luego suscribirse a los tópicos
        mqttService.connect()
            .compose(v -> { // Una vez conectado, obtener tópicos y suscribirse
                LOGGER.info("MQTT Client connected, fetching group topics for subscription...");
                return subscribeToAllGroupTopics();
            })
            .onSuccess(v -> {
                LOGGER.info("MQTT Client Verticle started and subscriptions completed successfully.");

                // Configurar un handler para mensajes recibidos (opcional, solo para log o procesar)
                mqttService.setMessageHandler(payload -> {
                    LOGGER.info("MQTT Verticle received message payload: {}", payload.encodePrettily());
                    // Aquí podrías reenviar el mensaje a otro Verticle/Servicio si fuera necesario
                });

                startPromise.complete();
            })
            .onFailure(err -> {
                LOGGER.error("Failed to start MQTT Client Verticle.", err);
                startPromise.fail(err);
            });
    }

    private Future<Void> subscribeToAllGroupTopics() {
        Promise<Void> promise = Promise.promise();

        dbService.getAllGroupTopics() // Necesitas implementar este método en DatabaseService
            .onFailure(err -> {
                LOGGER.error("Failed to retrieve group topics from database.", err);
                promise.fail(err); // Fallar si no podemos obtener los tópicos
            })
            .onSuccess(topics -> {
                if (topics == null || topics.isEmpty()) {
                    LOGGER.warn("No group topics found in database to subscribe to.");
                    promise.complete(); // Completar si no hay tópicos
                    return;
                }

                List<Future> subscriptionFutures = new ArrayList<>();
                for (String topic : topics) {
                    if (topic != null && !topic.isBlank()) {
                        // Suscribirse con QoS 1 por defecto
                        subscriptionFutures.add(mqttService.subscribe(topic, 1));
                    } else {
                        LOGGER.warn("Found null or blank topic in group list, skipping subscription.");
                    }
                }

                if (subscriptionFutures.isEmpty()) {
                     LOGGER.warn("No valid topics were found after filtering.");
                     promise.complete();
                     return;
                }

                // Esperar a que todas las suscripciones terminen
                CompositeFuture.all(new ArrayList<>(subscriptionFutures))
                    .onSuccess(v -> {
                        LOGGER.info("Successfully subscribed to {} group topics.", subscriptionFutures.size());
                        promise.complete();
                    })
                    .onFailure(err -> {
                        LOGGER.error("Failed to subscribe to one or more group topics.", err);
                        promise.fail(err); // Fallar si alguna suscripción falla
                    });
            });

        return promise.future();
    }


    public void stop(Promise<Void> stopPromise) {
        LOGGER.info("Stopping MQTT Client Verticle...");
        mqttService.disconnect()
            .onComplete(ar -> {
                if (ar.failed()) {
                    LOGGER.error("Error during MQTT disconnect.", ar.cause());
                } else {
                    LOGGER.info("MQTT client disconnected successfully.");
                }
                stopPromise.complete(); // Completar incluso si hubo error al desconectar
            });
    }
}