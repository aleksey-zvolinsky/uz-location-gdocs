package ua.com.kerriline.location.gdocs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	@Value("${google.project-name}")
	private String projectName = "kerriline-1101";
	@Value("${google.sheet.file-name}")
	private String spreadSheetName = "Дислокация";
	@Value("${google.sheet.tanks-worksheet-name}")
	private String tanksWorksheetName = "Вагоны";
	@Value("${google.sheet.binding-worksheet-name}")
	private String columnsWorksheetName = "Соответствие";
	@Value("${google.sheet.results-worksheet-name}")
	private String resultWorksheetName = "Результат";
	
	@Value("${google.service-account.email}")
	private String serviceAccountEmail;
	
	@Value("${google.service-account.p12-file}")
	private String p12FileName;

	@Autowired
	private GDocsSheetHelper helper;

	public void authorize() throws GeneralSecurityException, IOException {
		helper.setCredentials(serviceAccountEmail, p12FileName, projectName);
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
				LOG.debug(key+"->"+entry.getCustomElements().getValue(key));
				tank.put(key, entry.getCustomElements().getValue(key));
			}
			result.add(tank);
		}
		
		return result;
	}
	

	public List<Map<String, String>> readResultTanks() throws IOException, ServiceException {
		List<ListEntry> tanks = helper.getWorksheetData(spreadSheetName, resultWorksheetName);
		tanks.remove(0);

		List<Map<String, String>> result = new ArrayList<>();
		for (ListEntry entry : tanks) {
			Map<String, String> tank = new HashMap<>();
			for (String key : entry.getCustomElements().getTags()) {
				LOG.debug(key+"->"+entry.getCustomElements().getValue(key));
				tank.put(key, entry.getCustomElements().getValue(key));
			}
			result.add(tank);
		}
		
		return result;
	}
	

	/** 
	 * Removing tank from result worksheet
	 * 
	 * @param tank to delete
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void removeTank(String tank) throws IOException, ServiceException {
		List<ListEntry> results = helper.getWorksheetData(spreadSheetName, resultWorksheetName);
		Map<String, String> realColumns = helper.getRealColumns(spreadSheetName, resultWorksheetName);
		String key = realColumns.get("4");

		for (ListEntry entry : results) {			
			if(tank.equals(entry.getCustomElements().getValue(key))){
				entry.delete();
			}
		}
	}


	/**
	 * 
	 * @return mapping between mail columns and worksheet columns
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ServiceException
	 */
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

	public Map<String, String> getRealColumns() throws IOException, ServiceException {
		return helper.getRealColumns(spreadSheetName, resultWorksheetName);
	}


}
