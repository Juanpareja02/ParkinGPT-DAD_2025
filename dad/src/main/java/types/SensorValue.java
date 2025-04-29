package types;

import java.time.LocalDateTime;
import java.util.Objects;

/**
* Entidad que representa un valor registrado por un Sensor en un momento dado.
* Corresponde a la tabla 'SensorValue' en la base de datos.
*/
public class SensorValue {

 private Integer id; // PK
 private Integer idSensor; // FK a Sensor
 private Float valor; // valor (float en PDF, usando Double en Java)
 private LocalDateTime timestamp; // timestamp (datetime en PDF)

 public SensorValue() {
 }

 public SensorValue(Integer id, Integer idSensor, Float valor, LocalDateTime timestamp) {
     this.id = id;
     this.idSensor = idSensor;
     this.valor = valor;
     this.timestamp = timestamp;
 }

  // Constructor sin ID (útil al crear nuevos registros antes de insertarlos)
 public SensorValue(Integer idSensor, Float valor, LocalDateTime timestamp) {
     this.idSensor = idSensor;
     this.valor = valor;
     this.timestamp = timestamp;
 }


 // Getters y Setters
 public Integer getId() {
     return id;
 }

 public void setId(Integer id) {
     this.id = id;
 }

 public Integer getIdSensor() {
     return idSensor;
 }

 public void setIdSensor(Integer idSensor) {
     this.idSensor = idSensor;
 }

 public Float getValor() {
     return valor;
 }

 public void setValor(Float valor) {
     this.valor = valor;
 }

 public LocalDateTime getTimestamp() {
     return timestamp;
 }

 public void setTimestamp(LocalDateTime timestamp) {
     this.timestamp = timestamp;
 }

 // equals, hashCode, toString
 @Override
 public boolean equals(Object o) {
     if (this == o) return true;
     if (o == null || getClass() != o.getClass()) return false;
     SensorValue that = (SensorValue) o;
     return Objects.equals(id, that.id) &&
            Objects.equals(idSensor, that.idSensor) &&
            Objects.equals(valor, that.valor) &&
            Objects.equals(timestamp, that.timestamp);
 }

 @Override
 public int hashCode() {
     return Objects.hash(id, idSensor, valor, timestamp);
 }

 @Override
 public String toString() {
     return "SensorValue{" +
            "id=" + id +
            ", idSensor=" + idSensor +
            ", valor=" + valor +
            ", timestamp=" + timestamp +
            '}';
 }
}