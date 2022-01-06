package semantic.model;

/**
 * This is the list of functions you have to implement.
 */
public interface IModelFunctions
{
	/**
	 * Creates an instance of the class "Place" of your ontology
	 * @param name
	 * @return the URI of the instance
	 */
	public String createPlace(String name);
	
	/**
	 * Creates an instance of the "Instant" class of your ontology. You'll have to link it to 
	 * a data property that represents the timestamp, serialized as it is in the original data file.
	 * Only one instance should be created for each actual timestamp. 
	 * @param instant
	 * @return the URI of the created instant, null if it already existed
	 */
	public String createInstant(TimestampEntity instant);
	
	/**
	 * Returns the instant with the provided timestamp if it exists.
	 * @param instant
	 * @return the URI of the representation of the instant, null otherwise.
	 */
	public String getInstantURI(TimestampEntity instant);
	
	/**
	 * @param instantURI
	 * @return the value of the timestamp associated to the instant individual, null if the individual doesn't exist
	 */
	public String getInstantTimestamp(String instantURI);
	
	/**
	 * Creates an Observation of the provided value for the provided parameter
	 * at the provided time. It uses both object and data properties from the ontology 
	 * to link the observation to its value, instant, and parameter.
	 * @param value
	 * @param param
	 * @param instantURI
	 * @return the URI of the created observation
	 */
	public String createObs(String value, String paramURI, String instantURI);
}
