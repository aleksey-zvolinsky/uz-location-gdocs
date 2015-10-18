package ua.com.kerriline.location;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.util.ServiceException;

/**
 * 
 * @author Aleksey
 *
 */
@Component
public class GDocsSheet {

	private static final Log LOG = LogFactory.getLog(GDocsSheet.class);
	
	/** Our view of Google Spreadsheets as an authenticated Google user. */
	//TODO move to configuration
	private String projectName = "kerriline-1101";
	private String spreadSheetName = "Дислокация";
	private String tanksWorksheetName = "Вагоны";
	private String columnsWorksheetName = "Соответствие";
	private String resultWorksheetName = "Результат";
	
	@Value("${google.service-account.email}")
	private String serviceAccountEmail;
	
	@Value("${google.service-account.p12-file}")
	private String p12FileName;

	private GDocsSheetHelper helper;

	
	
	public GDocsSheet(String serviceAccountEmail, String p12FileName) {
		this.serviceAccountEmail = serviceAccountEmail;
		this.p12FileName = p12FileName;
	}

	public GDocsSheet() {
		helper = new GDocsSheetHelper(projectName);
	}

	public void authorize() throws GeneralSecurityException, IOException {
		helper.authorize(serviceAccountEmail, p12FileName);
	}
	


	/**
	 * 
	 * @return List<"FieldName" -> "FieldValue">
	 * @throws IOException
	 * @throws ServiceException
	 */
	public List<Map<String, String>> readTanks() throws IOException, ServiceException {

		List<ListEntry> tanks = helper.getWorksheetData(spreadSheetName, tanksWorksheetName);
		tanks.remove(0);

		List<Map<String, String>> result = new ArrayList<>();
		for (ListEntry entry : tanks) {
			Map<String, String> tank = new HashMap<>();
			for (String key : entry.getCustomElements().getTags()) {
				LOG.info(key+"->"+entry.getCustomElements().getValue(key));
				tank.put(key, entry.getCustomElements().getValue(key));
			}
			result.add(tank);
		}
		
		return result;
	}


	public Map<String, String> readColumns() throws MalformedURLException, IOException, ServiceException {

		List<ListEntry> list = helper.getWorksheetData(spreadSheetName, columnsWorksheetName);

		Map<String, String> result = null;
		ListEntry sheet = list.get(1);
		ListEntry mail = list.get(2);
		
		result = new HashMap<>();
		for (String key : sheet.getCustomElements().getTags()) {
			if(null != mail.getCustomElements().getValue(key) ){
				LOG.info(mail.getCustomElements().getValue(key)+"->"+sheet.getCustomElements().getValue(key));
				result.put(mail.getCustomElements().getValue(key), sheet.getCustomElements().getValue(key));
			}
		}
		
		return result;
	}

	public void writeData(Map<String, String> columns, List<Map<String, String>> newData) throws IOException, ServiceException {
		helper.writeData(spreadSheetName, resultWorksheetName, columns, newData);
	}

}
