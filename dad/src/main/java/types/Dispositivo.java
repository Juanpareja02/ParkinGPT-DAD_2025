package types;

import java.util.Objects;

/**
* Entidad que representa un Dispositivo físico (ej. ESP32).
* Corresponde a la tabla 'Dispositivo' en la base de datos.
*/
public class Dispositivo {

 private Long id; // PK
 private String plaza;
 private Long idGrupo; // FK a Grupo

 public Dispositivo() {
 }

 public Dispositivo(Long id, String plaza, Long idGrupo) {
     this.id = id;
     this.plaza = plaza;
     this.idGrupo = idGrupo;
 }

 // Getters y Setters
 public Long getId() {
     return id;
 }

 public void setId(Long id) {
     this.id = id;
 }

 public String getPlaza() {
     return plaza;
 }

 public void setPlaza(String plaza) {
     this.plaza = plaza;
 }

 public Long getIdGrupo() {
     return idGrupo;
 }

 public void setIdGrupo(Long idGrupo) {
     this.idGrupo = idGrupo;
 }

 // equals, hashCode, toString
 @Override
 public boolean equals(Object o) {
     if (this == o) return true;
     if (o == null || getClass() != o.getClass()) return false;
     Dispositivo that = (Dispositivo) o;
     return Objects.equals(id, that.id) &&
            Objects.equals(plaza, that.plaza) &&
            Objects.equals(idGrupo, that.idGrupo);
 }

 @Override
 public int hashCode() {
     return Objects.hash(id, plaza, idGrupo);
 }

 @Override
 public String toString() {
     return "Dispositivo{" +
            "id=" + id +
            ", plaza='" + plaza + '\'' +
            ", idGrupo=" + idGrupo +
            '}';
 }
}
