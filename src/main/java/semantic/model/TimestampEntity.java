package semantic.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TimestampEntity
{
	public String timestamp;
	public Date date;
	public String time;
	
	public TimestampEntity(String timestamp)
	{
		this.timestamp = timestamp;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		try 
		{
			this.date = df.parse(timestamp);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
	}
	
	public Date getDate()
	{
		return this.date;
	}
	
	public String getDay()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(this.date);
	}
	
	public String getTime()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		return formatter.format(this.date);
	}
	
	public int getHour()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("HH");
		return Integer.valueOf(formatter.format(this.date));	
	}
	
	public String getTimeStamp()
	{
		return this.timestamp;
	}
}
