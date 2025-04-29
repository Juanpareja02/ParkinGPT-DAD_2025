package types;
//package com.yourproject.parkingt.entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
* Entidad que representa el estado de un Actuador en un momento dado.
* Corresponde a la tabla 'ActuatorState' en la base de datos.
*/
public class LedState {

 private Integer id; // PK
 private Integer idActuador; // FK a Actuador
 private Boolean estado; // estado (boolean)
 private LocalDateTime timestamp; // timestamp (datetime)

 public LedState() {
 }

 public LedState(Integer id, Integer idActuador, Boolean estado, LocalDateTime timestamp) {
     this.id = id;
     this.idActuador = idActuador;
     this.estado = estado;
     this.timestamp = timestamp;
 }

  // Constructor sin ID (útil al crear nuevos registros antes de insertarlos)
  public LedState(Integer idActuador, Boolean estado, LocalDateTime timestamp) {
     this.idActuador = idActuador;
     this.estado = estado;
     this.timestamp = timestamp;
 }

 // Getters y Setters
 public Integer getId() {
     return id;
 }

 public void setId(Integer id) {
     this.id = id;
 }

 public Integer getIdActuador() {
     return idActuador;
 }

 public void setIdActuador(Integer idActuador) {
     this.idActuador = idActuador;
 }

 public Boolean getEstado() {
     return estado;
 }

 public void setEstado(Boolean estado) {
     this.estado = estado;
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
     LedState that = (LedState) o;
     return Objects.equals(id, that.id) &&
            Objects.equals(idActuador, that.idActuador) &&
            Objects.equals(estado, that.estado) &&
            Objects.equals(timestamp, that.timestamp);
 }

 @Override
 public int hashCode() {
     return Objects.hash(id, idActuador, estado, timestamp);
 }

 @Override
 public String toString() {
     return "ActuatorState{" +
            "id=" + id +
            ", idActuador=" + idActuador +
            ", estado=" + estado +
            ", timestamp=" + timestamp +
            '}';
 }
}