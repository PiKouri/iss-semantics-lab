package semantic;

import org.junit.Assert;
import org.junit.Test;

import semantic.controler.Controler;
import semantic.model.TimestampEntity;

public class TestModelFunctions
{
	@Test
	public void testPlaceCreation()
	{
		Controler c = new Controler();
		String jurassicParkURI = c.getCustomModel().createPlace("Jurassic park");
		String placeClassURI = c.getModel().getEntityURI("Lieu").get(0);
		Assert.assertTrue("L'entité créée n'est pas de classe Lieu", c.getModel().isOfType(jurassicParkURI, placeClassURI));
		Assert.assertTrue("Le lieu créé n'a pas de label pour indiquer son nom", c.getModel().listLabels(jurassicParkURI).contains("Jurassic park"));
	}
	
	@Test
	public void testInstantCreation()
	{
		Controler c = new Controler();
		TimestampEntity t = new TimestampEntity("2014-02-13T06:20:00");
		String instantURI = c.getCustomModel().createInstant(t);
		String instantClassURI = c.getModel().getEntityURI("Instant").get(0);
		String propertyURI =  c.getModel().getEntityURI("a pour timestamp").get(0);
		Assert.assertTrue("L'entité créée n'est pas de la classe Instant", c.getModel().isOfType(instantURI, instantClassURI));
		Assert.assertTrue("L'instant créé n'a pas le bon timestamp", c.getModel().hasDataPropertyValue(
				instantURI, propertyURI, "2014-02-13T06:20:00"));
	}
	
	@Test
	public void testInstantRetrieval()
	{
		Controler c = new Controler();
		TimestampEntity t = new TimestampEntity("2014-02-13T06:20:00");
		TimestampEntity t2 = new TimestampEntity("2015-02-13T06:20:00");
		String instantURI = c.getCustomModel().createInstant(t);
		Assert.assertTrue("La recherche d'un instant par timestamp ne retourne rien pour un instant sensé exister", c.getCustomModel().getInstantURI(t).equals(instantURI));
		Assert.assertNull("La recherche d'un instant par timestamp inexistant ne retourne pas un résultat null", c.getCustomModel().getInstantURI(t2));
	}
	
	@Test
	public void testTimestampRetrieval()
	{
		Controler c = new Controler();
		TimestampEntity t = new TimestampEntity("2014-02-13T06:20:00");
		String instantURI = c.getCustomModel().createInstant(t);
		Assert.assertTrue("Le timestamp ne correspond pas", c.getCustomModel().getInstantTimestamp(instantURI).equals("2014-02-13T06:20:00"));
	}
	
	@Test
	public void testObservationCreation()
	{
		Controler c = new Controler();
		TimestampEntity t = new TimestampEntity("2014-02-13T06:20:00");
		String instantURI = c.getCustomModel().createInstant(t);
		String paramURI = c.getModel().getEntityURI("Temperature").get(0);
		String value = "25.0";
		String obsURI = c.getCustomModel().createObs(value, paramURI, instantURI);
		// Entities relevant to the test
		String obsClassURI = c.getModel().getEntityURI("Observation").get(0);
		String hasDataValueURI = c.getModel().getEntityURI("a pour valeur").get(0);
		String datePropertyURI = c.getModel().getEntityURI("a pour date").get(0);
		String sensorURI = c.getModel().whichSensorDidIt("2014-02-13T06:20:00", paramURI);
		Assert.assertTrue("L'observation n'est pas une instance de la bonne classe", c.getModel().isOfType(obsURI, obsClassURI));
		Assert.assertTrue("L'observation n'a pas la bonne valeur",c.getModel().hasDataPropertyValue(obsURI, hasDataValueURI, value));
		Assert.assertTrue("L'observation n'a pas la bonne date",c.getModel().hasObjectProperty(obsURI, datePropertyURI, instantURI));
		Assert.assertTrue("L'observation n'est pas rattachée au bon capteur", c.getModel().hasSensorDoneIt(obsURI, sensorURI));
	}
}
