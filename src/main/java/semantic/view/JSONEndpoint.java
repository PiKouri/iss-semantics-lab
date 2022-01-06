package semantic.view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import semantic.model.ObservationEntity;
import semantic.model.TimestampEntity;

public class JSONEndpoint
{
	public static List<ObservationEntity> parseObservations(String path) throws IOException
	{
		  BufferedReader in = new BufferedReader(new FileReader(path));
		  String line;
		  line = in.readLine();
		  // Read observation entities, sorted by day
		  List<ObservationEntity> obsList = new ArrayList<ObservationEntity>();
		  while (line != null)
		  {
			  JsonParser parser = Json.createParser(new StringReader(line));
			  // At this point, event vaut START_OBJECT
			  Event e = parser.next();
			  String timestamp = null;
			  Float value = null;
			  // Each line exactly contains one json object
			  while(e != Event.END_OBJECT)
			  {
				  if(e == Event.KEY_NAME)
				  {
					  timestamp = parser.getString();
				  }
				  else if(e == Event.VALUE_STRING)
				  {
					  if(parser.getString().length() > 0)
					  {
						  value = Float.valueOf(parser.getString());
						  obsList.add(new ObservationEntity(value, new TimestampEntity(timestamp)));
					  }
				  }
				  e = parser.next();
			  }
			  line = in.readLine();
		  }
		  in.close();
		  return obsList;
	}
}
