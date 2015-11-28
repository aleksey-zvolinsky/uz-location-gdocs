package ua.com.kerriline.location.mail;

import java.io.BufferedReader;
import java.io.StringReader;

import org.springframework.stereotype.Component;

import ua.com.kerriline.location.data.Mileage;

@Component
public class MileageParser {
	private static final String IGNORE_TEXT = "Вагон не ремонтируется по пробегу";
	
	/**
	 * Parse from mail text to mileage  
	 * 
	 * @param message
	 * @param string 
	 * @return
	 */
	public Mileage parse(String subject, String message) {
		Mileage mileage = new Mileage(subject.split("-")[1]);
		if(message.contains(IGNORE_TEXT)) {
			return mileage;
		}
		
		BufferedReader reader = new BufferedReader(new StringReader(message));
		String mileageLine = reader.lines()
			.filter(s -> s.contains("ПРОБЕГ"))
			.findFirst().get();		
		
		mileage.setMileageDate(mileageLine.split("на|г.")[1].trim().replaceAll("  ", " "));
		mileage.setMileage(mileageLine.split("на|г.")[2].trim());
		
		String mileageRest = reader.lines()
				.filter(s -> s.contains("Норма  п р о б е г а"))
				.findFirst().get();
		
		mileage.setRestMileage(mileageRest.split("ОСТАЛОСЬ")[1].trim());
		  
		return mileage;		
	}
}
