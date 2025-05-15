package parkingpt;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import mqtt.ParkingMqttClient;
import vertx.BusinessRestVerticle;
import vertx.CrudRestVerticle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkingController extends AbstractVerticle {


private final Map<String, Float[]> rangosPermitidos = new HashMap<>();
private final Map<String, String> sensorToActuator = new HashMap<>();
private final Map<String, Boolean> actuadorEstado = new HashMap<>();
private final Map<String, String> actuadorCanal = new HashMap<>();
private final List<String> canalesMQTT = new ArrayList<>();

@Override
public void start(Promise<Void> startFuture) {
    inicializarConfiguracion();

    // Desplegar verticles necesarios
    vertx.deployVerticle(new ParkingMqttClient(this));
    vertx.deployVerticle(CrudRestVerticle.class.getName());
    vertx.deployVerticle(BusinessRestVerticle.class.getName());

    startFuture.complete();
    System.out.println("✅ ParkingController desplegó todos los verticles correctamente.");
}

/**
 * Evalúa si un valor de sensor está fuera del rango permitido
 */
public boolean evaluarSensor(String idSensor, float valor) {
    Float[] rango = rangosPermitidos.getOrDefault(idSensor, new Float[]{0f, 100f});
    return valor > rango[0] && valor < rango[1];
}

/**
 * Obtiene el ID del actuador asociado a un sensor
 */
public String getActuatorIdForSensor(String sensorId) {
    return sensorToActuator.get(sensorId);
}

/**
 * Alterna el estado del actuador (ON/OFF)
 */
public boolean getNextActuatorState(String actuatorId) {
    boolean estadoActual = actuadorEstado.getOrDefault(actuatorId, false);
    boolean nuevoEstado = !estadoActual;
    actuadorEstado.put(actuatorId, nuevoEstado);
    return nuevoEstado;
}

/**
 * Devuelve el canal MQTT asociado a un actuador
 */
public String getCanalActuador(String actuatorId) {
    return actuadorCanal.getOrDefault(actuatorId, "canal_actuador_default");
}

/**
 * Devuelve la lista de canales a los que suscribirse por MQTT
 */
public List<String> getMQTTChannels() {
    return canalesMQTT;
}

/**
 * Inicializa la configuración simulada de sensores y actuadores
 */
private void inicializarConfiguracion() {
    // Rango válido para sensor_1
    rangosPermitidos.put("sensor_1", new Float[]{15.0f, 30.0f});

    // Relación sensor -> actuador
    sensorToActuator.put("sensor_1", "act_1");

    // Estado inicial del actuador
    actuadorEstado.put("act_1", false);

    // Canal MQTT al que se suscribe el sensor
    canalesMQTT.add("grupo_1/canal_sensor");

    // Canal MQTT al que publica el actuador
    actuadorCanal.put("act_1", "grupo_1/canal_actuador");
}

}
