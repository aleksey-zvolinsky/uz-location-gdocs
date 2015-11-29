package ua.com.kerriline.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import ua.com.kerriline.location.data.Mileage;
import ua.com.kerriline.location.mail.MileageParser;

public class MileageParserTest {
	private static final String FILE_NAME = "mileageMail.txt";
	private static final String FILE_NAME_NO_DATA = "mileageMailNoData.txt";
	
	@Test
	public void testName() throws Exception {
		URL url = Thread.currentThread().getContextClassLoader().getResource(FILE_NAME_NO_DATA);
		assertNotNull("Sample file does not exist", url);
		
		String message = new String(Files.readAllBytes(Paths.get(url.toURI())), StandardCharsets.UTF_8);
		MileageParser parser = new MileageParser();
		Mileage mileage = parser.parse("2323-111", message);
		assertEquals("111", mileage.getTankNumber());
		assertEquals("This tank should not have mileage data", "", mileage.getMileage());
		
		url = Thread.currentThread().getContextClassLoader().getResource(FILE_NAME);
		message = new String(Files.readAllBytes(Paths.get(url.toURI())), StandardCharsets.UTF_8);
		
		mileage = parser.parse("2323-222", message);
		assertEquals("222", mileage.getTankNumber());
		assertNotNull("This tank should have mileage data", mileage.getMileage());
		assertEquals("0  км", mileage.getMileage());
		assertEquals("12 Августа 2015", mileage.getMileageDate());
		assertEquals("110.000 км", mileage.getRestMileage());
	}
}
