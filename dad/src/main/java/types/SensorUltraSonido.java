package types;

import java.util.Objects;

/**
* Entidad que representa un Sensor.
* Corresponde a la tabla 'Sensor' en la base de datos.
*/
public class SensorUltraSonido {

 private Long id; // PK
 private String nombre;
 private String tipo;
 private String identificador; // Identificador único del sensor físico
 private Long idDispositivo; // FK a Dispositivo
 private static Float thresholdDistancia; // Campo añadido para ParkinGPT que mide la distancia del coche al sensor

 public SensorUltraSonido() {
 }

 public SensorUltraSonido(Long id, String nombre, String tipo, String identificador, Long idDispositivo, Float thresholdDistancia) {
     this.id = id;
     this.nombre = nombre;
     this.tipo = tipo;
     this.identificador = identificador;
     this.idDispositivo = idDispositivo;
     this.thresholdDistancia = thresholdDistancia; // Inicializar campo añadido
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

  public static Float getThresholdDistancia() {
     return thresholdDistancia;
 }

 public void setThresholdDistancia(Float thresholdDistancia) {
     this.thresholdDistancia = thresholdDistancia;
 }

 // equals, hashCode, toString
 @Override
 public boolean equals(Object o) {
     if (this == o) return true;
     if (o == null || getClass() != o.getClass()) return false;
     SensorUltraSonido sensor = (SensorUltraSonido) o;
     return Objects.equals(id, sensor.id) &&
            Objects.equals(nombre, sensor.nombre) &&
            Objects.equals(tipo, sensor.tipo) &&
            Objects.equals(identificador, sensor.identificador) &&
            Objects.equals(idDispositivo, sensor.idDispositivo) &&
            Objects.equals(thresholdDistancia, sensor.thresholdDistancia); // Incluir campo añadido
 }

 @Override
 public int hashCode() {
     return Objects.hash(id, nombre, tipo, identificador, idDispositivo, thresholdDistancia); // Incluir campo añadido
 }

 @Override
 public String toString() {
     return "Sensor{" +
            "id=" + id +
            ", nombre='" + nombre + '\'' +
            ", tipo='" + tipo + '\'' +
            ", identificador='" + identificador + '\'' +
            ", idDispositivo=" + idDispositivo +
            ", thresholdDistancia=" + thresholdDistancia + // Incluir campo añadido
            '}';
 }
}