package mqtt;

import com.google.gson.JsonObject;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

public interface MqttService {

    /**
     * Crea una instancia del servicio MQTT.
     * @param vertx Instancia de Vertx.
     * @param config Configuración de la aplicación (para host, puerto, etc.).
     * @return Instancia de MqttService.
     */
    static MqttService create(Vertx vertx, JsonObject config) {
        return new MqttServiceImpl(vertx, config);
    }

    /**
     * Conecta al broker MQTT.
     * @return Future que se completa al conectar o falla en caso de error.
     */
    Future<Void> connect();

    /**
     * Desconecta del broker MQTT.
      * @return Future que se completa al desconectar.
     */
    Future<Void> disconnect();

    /**
     * Publica un comando de estado a un actuador específico en un tópico dado.
     * El formato del mensaje será: { "actuatorId": "identificador", "state": true/false }
     *
     * @param topic Tópico MQTT al que publicar (normalmente el canal_mqtt del grupo).
     * @param actuatorIdentifier El 'identificador' único del actuador.
     * @param state El estado deseado (true para ON/activado, false para OFF/desactivado).
     * @return Future que se completa con el ID del mensaje publicado o falla en caso de error.
     */
    Future<Integer> publishActuatorCommand(String topic, String actuatorIdentifier, boolean state);

    /**
     * Suscribe el cliente a un tópico específico.
     * @param topic Tópico al que suscribirse.
     * @param qos Nivel de calidad de servicio (0, 1 o 2).
     * @return Future que se completa con el ID de suscripción o falla en caso de error.
     */
    Future<Integer> subscribe(String topic, int qos);

    /**
     * Configura un manejador para los mensajes entrantes en los tópicos suscritos.
     * @param handler Función a ejecutar cuando llega un mensaje.
     */
    void setMessageHandler(java.util.function.Consumer<JsonObject> handler);

     /**
     * Indica si el cliente está actualmente conectado al broker.
     * @return true si está conectado, false en caso contrario.
     */
    boolean isConnected();

}