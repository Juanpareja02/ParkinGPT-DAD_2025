package types;

import java.time.LocalDateTime;

public class SensorValue {
	private Long id;
    private int sensorId; // Corresponde a id_sensor en DB
    private Double valor;
    private LocalDateTime timestamp;

    // Constructores, Getters y Setters
    // Necesitarás un constructor vacío para algunas librerías JSON/ORM
    public SensorValue() {}

    public SensorValue(int sensorId, Double valor) {
        this.sensorId = sensorId;
        this.valor = valor;
        // Timestamp se puede poner aquí o dejar que la DB lo ponga
    }


    public Long getId() { 
    	return id;
    	}
    public void setId(Long id) { 
    	this.id = id;
    	}
    public int getSensorId() { 
    	return sensorId; 
    	}
    public void setSensorId(int sensorId) {
    	this.sensorId = sensorId; 
    	}
    public Double getValor() { 
    	return valor; 
    	}
    public void setValor(Double valor) { 
    	this.valor = valor; 
    	}
    public LocalDateTime getTimestamp() {
    	return timestamp; 
    	}
    public void setTimestamp(LocalDateTime timestamp) {
    	this.timestamp = timestamp; 
    	}

}
