package parkingpt;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import mqtt.ParkingMqttClient;
import vertx.BusinessRestVerticle;
import vertx.CrudRestVerticle;

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

    @Override
    public void start(Promise<Void> startFuture) {
        inicializarConfiguracion();

        // Desplegar verticles
        vertx.deployVerticle(new ParkingMqttClient(this));
        vertx.deployVerticle(CrudRestVerticle.class.getName());
        vertx.deployVerticle(BusinessRestVerticle.class.getName());

        System.out.println("ParkingController desplegó todos los verticles correctamente.");
        startFuture.complete();
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

    /** Configuración mock de sensores, actuadores y canales */
    private void inicializarConfiguracion() {
        // Rango para sensor 1
        rangosPermitidos.put(1, new Float[]{10f, 30f});

        // Sensor 1 → actuador rojo "act_1"
        sensorToActuatorOut.put(1, "act_1");
        // Sensor 1 → actuador verde "act_2"
        sensorToActuatorIn.put(1, "act_2");

        // Estados iniciales
        actuadorEstado.put("act_1", false);
        actuadorEstado.put("act_2", false);

        // Canal de sensor
        canalesMQTT.add("grupo_1/canal_sensor");

        // Canales de actuadores
        actuadorCanal.put("act_1", "grupo_1/canal_actuador_rojo");
        actuadorCanal.put("act_2", "grupo_1/canal_actuador_verde");
    }
}
