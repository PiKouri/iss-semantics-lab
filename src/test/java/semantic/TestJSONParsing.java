package semantic;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import semantic.model.ObservationEntity;
import semantic.view.JSONEndpoint;

public class TestJSONParsing
{
	@Test
	public void testParseTemperature() 
	{
		List<ObservationEntity> obslist = null;
		try
		{
			obslist = JSONEndpoint.parseObservations("./src/test/temperature.txt");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		Assert.assertNotNull(obslist);
		Assert.assertNotEquals(0, obslist.size());
	}
}
