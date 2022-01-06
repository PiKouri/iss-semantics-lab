package semantic.model;

import java.util.List;

/**
 * Provides all the functions you should need in the scope of this lab
 */
public interface IConvenienceInterface
{
	/**
	 * There are multiple temperature sensors on the platform. This method indicates
	 * which one performed a measure based on its timestamp.
	 * @param obs, useful to get the timestamp
	 * @param paramURI, the URI of the observed parameter
	 * @return the URI of the sensor having performed the measure.
	 */
	public String whichSensorDidIt(String timestamp, String paramURI);
	/**
	 * Specify, for a given observation, that it came from the provided sensor.
	 * @param obsURI
	 * @param sensorURI
	 */
	public void addObservationToSensor(String obsURI, String sensorURI);
	
	/**
	 * @param obsURI
	 * @param sensorURI
	 * @return true if the observation is linked to the sensor. Useful for test purpose mainly.
	 */
	public boolean hasSensorDoneIt(String obsURI, String sensorURI);
	
	/**
	 * @param label
	 * @return all the URIs of instances of the class of concepts associated to this label.
	 */
	public List<String> getInstancesURI(String classURI);
	
	/**
	 * @param label
	 * @return the URI of the classes associated to this label
	 */
	public List<String> getEntityURI(String label);
	/**
	 * Tests whether the provided instance is a subclass (recursively) of the provided type. 
	 * It means that this function will always return true if the type is owl:Thing.
	 * @param instanceURI
	 * @param typeURI
	 * @return
	 */
	public boolean isOfType(String instanceURI, String typeURI);
	/**
	 * Creates an instance of the provided type, with the provided label.
	 * @param label
	 * @param the URI of the type
	 * @return the URI of the created individual
	 */
	public String createInstance(String label, String type);
	/**
	 * Adds a triple in the knowledge base <subject, property, object>
	 * @param subjectURI
	 * @param propertyURI
	 * @param objectURI
	 */
	public void addObjectPropertyToIndividual(String subjectURI, String propertyURI, String objectURI);
	/**
	 * Adds a triple in the knowledge base <subject, property, data>
	 * @param subjectURI
	 * @param propertyURI
	 * @param data
	 */
	public void addDataPropertyToIndividual(String subjectURI, String propertyURI, String data);
	
	/**
	 * Generates a .ttl file containing a serialization of the model.
	 * @param path
	 */
	public void exportModel(String path);
	
	/**
	 * @param subjectURI
	 * @param dataPropertyURI
	 * @param dataValue
	 * @return if provided subject has a certain value for provided property
	 */
	public boolean hasDataPropertyValue(String subjectURI, String dataPropertyURI, String dataValue);
	
	/**
	 * @param subjectURI
	 * @param propertyURI
	 * @param objectURI
	 * @return if provided subject has a certain relationship with another entity
	 */
	public boolean hasObjectProperty(String subjectURI, String propertyURI, String objectURI);
	
	/**
	 * @param entityURI
	 * @return a list of the labels of the entity
	 */
	public List<String> listLabels(String entityURI);
	
	/**
	 * @param entityURI
	 * @return A list of couples <property, object>
	 */
	public List<List<String>> listProperties(String entityURI);
}
