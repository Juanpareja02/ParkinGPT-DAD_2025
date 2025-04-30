package parkingpt;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import mqtt.ParkingMqttClient;
import vertx.BusinessRestVerticle;
import vertx.CrudRestVerticle;

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

        inicializarConfiguracion();

        // Desplegar verticles necesarios
        vertx.deployVerticle(new ParkingMqttClient(this));
        vertx.deployVerticle(CrudRestVerticle.class.getName());
        vertx.deployVerticle(BusinessRestVerticle.class.getName());

        startFuture.complete();
        System.out.println("✅ ParkingController desplegó todos los verticles correctamente.");
    }

    // ===== Lógica de negocio simulada =====
    public boolean evaluarSensor(String idSensor, float valor) {
        Float[] rango = rangosPermitidos.getOrDefault(idSensor, new Float[]{0f, 100f});
        return valor < rango[0] || valor > rango[1];
    }

    public String getActuatorIdForSensor(String sensorId) {
        return sensorToActuator.get(sensorId);
    }

    public boolean getNextActuatorState(String actuatorId) {
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

    // ===== Configuración simulada =====
    private void inicializarConfiguracion() {
        rangosPermitidos.put("sensor_1", new Float[]{15.0f, 30.0f});
        sensorToActuator.put("sensor_1", "act_1");
        actuadorEstado.put("act_1", false);
        canalesMQTT.add("grupo_1/canal_sensor");
        actuadorCanal.put("act_1", "grupo_1/canal_actuador");
    }
}
