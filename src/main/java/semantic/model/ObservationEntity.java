package semantic.model;

public class ObservationEntity
{
	private Float value;
	private TimestampEntity timestamp;
	
	public ObservationEntity(Float value, TimestampEntity timestamp)
	{
		this.value = value;
		this.timestamp = timestamp;
	}
	
	public Float getValue()
	{
		return value;
	}
	public void setValue(Float value)
	{
		this.value = value;
	}
	public TimestampEntity getTimestamp()
	{
		return timestamp;
	}
	public void setTimestamp(TimestampEntity timestamp)
	{
		this.timestamp = timestamp;
	}
	
	public String toString()
	{
		return "Observation entity : "+value+" at "+timestamp.getTimeStamp();
	}
	
	
}
