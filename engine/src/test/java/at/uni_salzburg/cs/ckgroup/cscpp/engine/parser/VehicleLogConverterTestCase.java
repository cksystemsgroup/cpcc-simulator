package at.uni_salzburg.cs.ckgroup.cscpp.engine.parser;

import org.json.simple.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import at.uni_salzburg.cs.ckgroup.cscpp.engine.json.VehicleLogConverter;

public class VehicleLogConverterTestCase {
	
	public static final String LOG_MESSAGES =
		"1330276517360 upload to http://localhost:8080/engine\n" +
		"1330276517361 resume at (47.82202216,13.04082111,0.00000000)\n" +
		"1330276569871 position at (47.82202216,13.04082111,0.89900000)\n" +
		"1330276578873 position at (47.82202216,13.04082111,19.46900000)\n" +
		"1330276583873 position at (47.82203197,13.04083557,19.94100000)\n" +
		"1330276675890 position at (47.82203728,13.04263267,19.95900000)\n" +
		"1330276685893 position at (47.82196764,13.04263820,19.95800000)\n" +
		"1330276731975 suspend at (47.82196746,13.04178566,19.94200000)\n" +
		"1330276734547 resume at (47.82196723,13.04172628,19.94200000)\n" +
		"1330276753085 suspend at (47.82196558,13.04121076,19.95300000)\n";
	
	public static final String expectedString = "[{\"longitude\":\"13.04082111\",\"latitude\":\"47.82202216\",\"motion\":\"physical\"},{\"longitude\":\"13.04082111\",\"latitude\":\"47.82202216\",\"motion\":\"physical\"},{\"longitude\":\"13.04082111\",\"latitude\":\"47.82202216\",\"motion\":\"physical\"},{\"longitude\":\"13.04083557\",\"latitude\":\"47.82203197\",\"motion\":\"physical\"},{\"longitude\":\"13.04263267\",\"latitude\":\"47.82203728\",\"motion\":\"physical\"},{\"longitude\":\"13.04263820\",\"latitude\":\"47.82196764\",\"motion\":\"physical\"},{\"longitude\":\"13.04178566\",\"latitude\":\"47.82196746\",\"motion\":\"dead\"},{\"longitude\":\"13.04172628\",\"latitude\":\"47.82196723\",\"motion\":\"physical\"},{\"longitude\":\"13.04121076\",\"latitude\":\"47.82196558\",\"motion\":\"dead\"}]";

	@Test
	public void test01() {
		VehicleLogConverter c = new VehicleLogConverter();
		
		JSONArray a = c.convertToVirtualVehiclePath(LOG_MESSAGES);
		
		Assert.assertEquals(expectedString, a.toString());
		System.out.println(a);
	}

}
