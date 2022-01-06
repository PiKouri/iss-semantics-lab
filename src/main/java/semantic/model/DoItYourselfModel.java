package semantic.model;

import java.util.List;

public class DoItYourselfModel implements IModelFunctions
{
	IConvenienceInterface model;

	public DoItYourselfModel(IConvenienceInterface m) {
		this.model = m;
	}

	@Override
	public String createPlace(String name) {
		String type = model.getEntityURI("Place").get(0);
		return model.createInstance(name, type);
	}

	@Override
	public String createInstant(TimestampEntity instant) {
		String type = model.getEntityURI("Instant").get(0);
		// This label was found in tp-iss.ttl
		String timestampType = model.getEntityURI("a pour timestamp").get(0);
		List<String> instances = model.getInstancesURI(type);
		// Check that the instant does not already exist
		for (String e : instances) {
			if (model.hasDataPropertyValue(e, timestampType, instant.getTimeStamp())) 
				// Returns null if existing
				return null;
		}
		// The instant does not exist 
		// 	=> Creates the instant and adds the timestamp data property
		String uri = model.createInstance(instant.getTimeStamp(), type);
		model.addDataPropertyToIndividual(uri, timestampType, instant.getTimeStamp());
		return uri;
	}

	@Override
	public String getInstantURI(TimestampEntity instant) {
		String type = model.getEntityURI("Instant").get(0);
		// This label was found in tp-iss.ttl
		String timestampType = model.getEntityURI("a pour timestamp").get(0);
		List<String> instances = model.getInstancesURI(type);
		for (String e : instances) {
			// Searches for an Instant with the given timestamp value
			if (model.hasDataPropertyValue(e, timestampType, instant.getTimeStamp())) 
				return e;
		}
		return null;
	}

	@Override
	public String getInstantTimestamp(String instantURI)
	{
		// This label was found in tp-iss.ttl
		String timestampType = model.getEntityURI("a pour timestamp").get(0);
		// Gets the properties associated to the given Instant
		// If the instant does not exist, the properties will be empty
		List<List<String>> properties = model.listProperties(instantURI);
		if (properties.isEmpty())
			return null;
		else {
			// Gets the couples of data property - value
			// And finds the timestamp data property
			for (List<String> property : properties) {
				if (property.isEmpty() || !property.get(0).equals(timestampType))
					continue;
				else if (property.size() != 2) 
					break;
				else
					// Gets the timestamp data property
					return property.get(1);
			}
			return null;
		}
	}

	@Override
	public String createObs(String value, String paramURI, String instantURI) {
		String type = model.getEntityURI("Observation").get(0);
		
		// Create an Observation
		String uri = model.createInstance("Obs"+paramURI+value+instantURI, type);
		
		// Gets the timestamp of the given Instant
		String timestamp = getInstantTimestamp(instantURI);
		
		// Gets the sensor that did the observation and associates them
		String sensor = this.model.whichSensorDidIt(timestamp, paramURI);
		this.model.addObservationToSensor(uri, sensor);

		// This label was found in tp-iss.ttl
		String hasValueURI = model.getEntityURI("a pour valeur").get(0);
		model.addDataPropertyToIndividual(uri, hasValueURI, value);

		// This label was found in tp-iss.ttl
		String measureURI = model.getEntityURI("mesure").get(0);
		model.addObjectPropertyToIndividual(uri, measureURI, paramURI);

		// This label was found in tp-iss.ttl
		String instantObjectProperty = model.getEntityURI("a pour date").get(0);
		model.addObjectPropertyToIndividual(uri, instantObjectProperty, instantURI);

		return uri;
	}
}
