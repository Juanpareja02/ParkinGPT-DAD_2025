package types;

import java.util.Objects;

/**
* Entidad que representa un Grupo de dispositivos.
* Corresponde a la tabla 'Grupo' en la base de datos.
*/
public class Grupo {

 private Long id; // PK
 private String nombre;
 private String canalMqtt; // canal_mqtt

 // Constructor por defecto (necesario para algunos frameworks como Jackson)
 public Grupo() {
 }

 // Constructor con todos los campos
 public Grupo(Long id, String nombre, String canalMqtt) {
     this.id = id;
     this.nombre = nombre;
     this.canalMqtt = canalMqtt;
 }

 // Getters y Setters
 public Long getId() {
     return id;
 }

 public void setId(Long id) {
     this.id = id;
 }

 public String getNombre() {
     return nombre;
 }

 public void setNombre(String nombre) {
     this.nombre = nombre;
 }

 public String getCanalMqtt() {
     return canalMqtt;
 }

 public void setCanalMqtt(String canalMqtt) {
     this.canalMqtt = canalMqtt;
 }

 // equals, hashCode, toString
 @Override
 public boolean equals(Object o) {
     if (this == o) return true;
     if (o == null || getClass() != o.getClass()) return false;
     Grupo grupo = (Grupo) o;
     return Objects.equals(id, grupo.id) &&
            Objects.equals(nombre, grupo.nombre) &&
            Objects.equals(canalMqtt, grupo.canalMqtt);
 }

 @Override
 public int hashCode() {
     return Objects.hash(id, nombre, canalMqtt);
 }

 @Override
 public String toString() {
     return "Grupo{" +
            "id=" + id +
            ", nombre='" + nombre + '\'' +
            ", canalMqtt='" + canalMqtt + '\'' +
            '}';
 }
}