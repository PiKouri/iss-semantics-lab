package semantic.controler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import semantic.model.IConvenienceInterface;
import semantic.model.IModelFunctions;
import semantic.model.ObservationEntity;

public class DoItYourselfControl implements IControlFunctions
{
	private IConvenienceInterface model;
	private IModelFunctions customModel;

	public DoItYourselfControl(IConvenienceInterface model, IModelFunctions customModel)
	{
		this.model = model;
		this.customModel = customModel;
	}

	@Override
	public void instantiateObservations(List<ObservationEntity> obsList,
			String paramURI) {
		// Map to avoid trying to create multiple instant instance for a same timestamp
		// (timestamp, associated instant's URI)
		Map<String, String> mapTimestampURI = new HashMap<String, String>(); 
		for (ObservationEntity obs : obsList) {

			String timestamp = obs.getTimestamp().getTimeStamp();

			String instantURI = null;

			if (mapTimestampURI.containsKey(timestamp))
				instantURI = mapTimestampURI.get(timestamp);
			else {
				instantURI = customModel.createInstant(obs.getTimestamp());
				mapTimestampURI.put(timestamp, instantURI);
			}

			customModel.createObs(obs.getValue().toString(), paramURI, instantURI);
		}
	}
}
