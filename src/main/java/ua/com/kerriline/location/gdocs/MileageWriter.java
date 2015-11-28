package ua.com.kerriline.location.gdocs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.util.PreconditionFailedException;
import com.google.gdata.util.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ua.com.kerriline.location.data.Mileage;

@Component
public class MileageWriter {
	
	private static final String MILEAGE_UPDATE = "ПРОБЕГ-ОБНОВЛЕННО";
	private static final String MILEAGE_REST = "ПРОБЕГ-ОСТАЛОСЬ";
	private static final String MILEAGE_DATE = "ПРОБЕГ-ДАТА";
	private static final String MILEAGE = "ПРОБЕГ-ТЕКУЩИЙ";

	private static final Log LOG = LogFactory.getLog(MileageWriter.class);
	
	private DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	@Autowired 
	private GDocsSheet gdocs; 
		
	public void writeAll(List<Mileage> mileage) throws IOException, ServiceException, GeneralSecurityException {
		gdocs.authorize();
		
		int i = 0;
		for (Mileage record: mileage) {
			LOG.info("Updating record " + ++i + ": " + record);
			update(record, 1);
		}
	}

	private void update(Mileage record, int attempt) throws IOException, ServiceException, MalformedURLException {
		ListEntry existEntry = gdocs.searchByTank(record.getTankNumber());
		if (null == existEntry){
			LOG.error("Failed to find record with " + record.getTankNumber() + " tanknumber");
		} else {
			gdocs.setValue(existEntry, MILEAGE, record.getMileage());
			gdocs.setValue(existEntry, MILEAGE_DATE, record.getMileageDate());
			gdocs.setValue(existEntry, MILEAGE_REST, record.getRestMileage());
			gdocs.setValue(existEntry, MILEAGE_UPDATE, df.format(new Date()));
			existEntry.setEtag("*");
			try {
				existEntry.update();
			} catch (PreconditionFailedException e) {
				if(attempt <= 2) {
					LOG.error("Update failed on " + attempt + " attempt, repeating ", e);				
					update(record, attempt++);
				} else {
					throw e;
				}
			}
		}
	}
}
