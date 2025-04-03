package types;

import java.util.Objects;

public class Led {
	private Integer id;
	private Boolean encendido;
	private Long timestamp;
	
	public Led(Integer id, Boolean encendido, Long timestamp) {
		super();
		this.id = id;
		this.encendido = encendido;
		this.timestamp = timestamp;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Boolean getEncendido() {
		return encendido;
	}
	public void setEncendido(Boolean encendido) {
		this.encendido = encendido;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public int hashCode() {
		return Objects.hash(encendido, id, timestamp);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Led other = (Led) obj;
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
		if (encendido == null) {
			if (other.encendido != null)
				return false;
		}else if (!encendido.equals(other.encendido))
			return false;
		return true;
	}
	
}
