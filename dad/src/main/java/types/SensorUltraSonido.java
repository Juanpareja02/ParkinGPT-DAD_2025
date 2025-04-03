package types;

import java.util.Objects;

public class SensorUltraSonido {
	private Integer id;
	private Boolean ocupado;
	private Long timestamp;
	
	public SensorUltraSonido(Integer id, Boolean ocupado, Long timestamp) {
		super();
		this.id = id;
		this.ocupado = ocupado;
		this.timestamp = timestamp;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Boolean getOcupado() {
		return ocupado;
	}
	public void setOcupado(Boolean ocupado) {
		this.ocupado = ocupado;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public int hashCode() {
		return Objects.hash(id, ocupado, timestamp);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SensorUltraSonido other = (SensorUltraSonido) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (ocupado == null) {
			if (other.ocupado != null)
				return false;
		}else if (!ocupado.equals(other.ocupado))
			return false;
		return true;
	}
	
}
