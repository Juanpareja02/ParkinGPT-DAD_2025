package types;

import java.util.Objects;

/**
* Entidad que representa un Actuador (ej. LED, relé).
* Corresponde a la tabla 'Actuador' en la base de datos.
*/
public class Led {

 private Long id; // PK
 private String nombre;
 private String tipo;
 private String identificador; // Identificador único del actuador físico
 private Long idDispositivo; // FK a Dispositivo

 public Led() {
 }

 public Led(Long id, String nombre, String tipo, String identificador, Long idDispositivo) {
     this.id = id;
     this.nombre = nombre;
     this.tipo = tipo;
     this.identificador = identificador;
     this.idDispositivo = idDispositivo;
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

 public String getTipo() {
     return tipo;
 }

 public void setTipo(String tipo) {
     this.tipo = tipo;
 }

 public String getIdentificador() {
     return identificador;
 }

 public void setIdentificador(String identificador) {
     this.identificador = identificador;
 }

 public Long getIdDispositivo() {
     return idDispositivo;
 }

 public void setIdDispositivo(Long idDispositivo) {
     this.idDispositivo = idDispositivo;
 }

 // equals, hashCode, toString
 @Override
 public boolean equals(Object o) {
     if (this == o) return true;
     if (o == null || getClass() != o.getClass()) return false;
     Led actuador = (Led) o;
     return Objects.equals(id, actuador.id) &&
            Objects.equals(nombre, actuador.nombre) &&
            Objects.equals(tipo, actuador.tipo) &&
            Objects.equals(identificador, actuador.identificador) &&
            Objects.equals(idDispositivo, actuador.idDispositivo);
 }

 @Override
 public int hashCode() {
     return Objects.hash(id, nombre, tipo, identificador, idDispositivo);
 }

 @Override
 public String toString() {
     return "Actuador{" +
            "id=" + id +
            ", nombre='" + nombre + '\'' +
            ", tipo='" + tipo + '\'' +
            ", identificador='" + identificador + '\'' +
            ", idDispositivo=" + idDispositivo +
            '}';
 }
}