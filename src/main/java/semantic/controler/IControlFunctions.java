package semantic.controler;

import java.util.List;

import semantic.model.ObservationEntity;

public interface IControlFunctions 
{
	/**
	 * This function parses the list of observations extracted from the dataset, 
	 * and instanciates them in the knowledge base. 
	 * @param obsList
	 * @param phenomenonURI
	 */
	public void instantiateObservations(List<ObservationEntity> obsList, String phenomenonURI);
}
