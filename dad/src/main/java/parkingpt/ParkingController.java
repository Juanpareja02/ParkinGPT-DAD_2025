package parkingpt;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import mqtt.ParkingMqttClient;
import vertx.MySQLVerticle;

import java.util.*;

public class ParkingController extends AbstractVerticle {

    // Mocks de base de datos y configuración de sensores/actuadores
    private Map<String, Float[]> rangosPermitidos = new HashMap<>();
    private Map<String, String> sensorToActuator = new HashMap<>();
    private Map<String, Boolean> actuadorEstado = new HashMap<>();
    private Map<String, String> actuadorCanal = new HashMap<>();
    private List<String> canalesMQTT = new ArrayList<>();

    @Override
    public void start(Promise<Void> startFuture) {

        // Simulación de configuración
        inicializarConfiguracion();

        vertx.createHttpServer().requestHandler(r -> {
            r.response().end("<h1>Bienvenido a ParkinGPT</h1>Aplicación para matrakas.");
        }).listen(8089, result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(result.cause());
            }
        });

        // Desplegar con este controller como dependencia
        vertx.deployVerticle(new ParkingMqttClient(this));
        vertx.deployVerticle(MySQLVerticle.class.getName());
    }

    // Método simulado que decide si activar un actuador en función del valor del sensor
    public boolean evaluarSensor(String idSensor, float valor) {
        Float[] rango = rangosPermitidos.getOrDefault(idSensor, new Float[]{0f, 100f});
        return valor < rango[0] || valor > rango[1];
    }

    public String getActuatorIdForSensor(String sensorId) {
        return sensorToActuator.get(sensorId);
    }

    public boolean getNextActuatorState(String actuatorId) {
        // Simula un toggle de estado
        boolean estadoActual = actuadorEstado.getOrDefault(actuatorId, false);
        boolean nuevoEstado = !estadoActual;
        actuadorEstado.put(actuatorId, nuevoEstado);
        return nuevoEstado;
    }

    public String getCanalActuador(String actuatorId) {
        return actuadorCanal.getOrDefault(actuatorId, "canal_actuador_default");
    }

    public List<String> getMQTTChannels() {
        return canalesMQTT;
    }

    // Inicialización simulada de relaciones
    private void inicializarConfiguracion() {
        // Sensores con su rango permitido
        rangosPermitidos.put("sensor_1", new Float[]{15.0f, 30.0f});

        // Relación sensor-actuador
        sensorToActuator.put("sensor_1", "act_1");

        // Estados iniciales
        actuadorEstado.put("act_1", false);

        // Canales MQTT para el grupo del sensor y del actuador
        canalesMQTT.add("grupo_1/canal_sensor"); // para recibir mensajes de sensores
        actuadorCanal.put("act_1", "grupo_1/canal_actuador");
    }
}
