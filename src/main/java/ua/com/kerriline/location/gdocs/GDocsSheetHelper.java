package ua.com.kerriline.location.gdocs;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gdata.client.spreadsheet.ListQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;

public class GDocsSheetHelper {
	
	private static final String TANK = "ДАННЫЕ О ВАГОНЕ";
	private static final String SPREADSHEET_SERVICE_URL = "https://spreadsheets.google.com/feeds/spreadsheets/private/full";
	private final static List<String> SCOPES = Arrays.asList("https://spreadsheets.google.com/feeds https://docs.google.com/feeds https://www.googleapis.com/auth/drive https://www.googleapis.com/auth/drive.appdata https://www.googleapis.com/auth/drive.apps.readonly https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/drive.metadata");
	private static final Log LOG = LogFactory.getLog(GDocsSheetHelper.class);
	private SpreadsheetService service;
	
	public void setCredentials(String serviceAccountEmail, String p12FileName, String projectName) throws GeneralSecurityException, IOException {
		service = new SpreadsheetService(projectName);
		service.setOAuth2Credentials(authorize(serviceAccountEmail, p12FileName));
	}
	
	
	public Credential authorize(String serviceAccountEmail, String p12FileName) throws GeneralSecurityException, IOException {
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		
		String file = Thread.currentThread().getContextClassLoader().getResource(p12FileName).getFile();
		
		// Build service account credential.
		GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
		    .setJsonFactory(jsonFactory)
		    .setServiceAccountId(serviceAccountEmail)
		    .setServiceAccountScopes(SCOPES)
		    .setServiceAccountPrivateKeyFromP12File(new File(file))
		    .build();
		return credential;

	}
	
	
	/**
	 * Lists all rows in the spreadsheet.
	 * @param recordsFeedUrl 
	 * @return 
	 * 
	 * @throws ServiceException
	 *             when the request causes an error in the Google Spreadsheets
	 *             service.
	 * @throws IOException
	 *             when an error occurs in communication with the Google
	 *             Spreadsheets service.
	 */
	public List<ListEntry> readAllEntries(URL listFeedUrl) throws IOException, ServiceException {
		
		ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);

		for (ListEntry entry : feed.getEntries()) {
			printAndCacheEntry(entry);
		}

		if (feed.getEntries().size() == 0) {
			LOG.error("No entries yet!");
		}

		return feed.getEntries();
	}
	

	public List<ListEntry> getWorksheetData(String spreadSheetName, String tanksWorksheetName) throws IOException, ServiceException {
		WorksheetEntry tanksWorksheet = getWorkSheet(spreadSheetName, tanksWorksheetName);		

		List<ListEntry> list = readAllEntries(tanksWorksheet.getListFeedUrl());
		return list;
	}
	
	
	  /**
	   * Prints the entire list entry, in a way that mildly resembles what the
	   * actual XML looks like.
	   * 
	   * In addition, all printed entries are cached here. This way, they can be
	   * updated or deleted, without having to retrieve the version identifier again
	   * from the server.
	   * 
	   * @param entry the list entry to print
	   */
	  public void printAndCacheEntry(ListEntry entry) {

	    // We only care about the entry id, chop off the leftmost part.
	    // I.E., this turns http://spreadsheets.google.com/..../cpzh6 into cpzh6.
	    String id = entry.getId().substring(entry.getId().lastIndexOf('/') + 1);

	    

	    LOG.debug("-- id: " + id + "  title: " + entry.getTitle().getPlainText());

	    for (String tag : entry.getCustomElements().getTags()) {
	    	LOG.debug("     <gsx:" + tag + ">"
	          + entry.getCustomElements().getValue(tag) + "</gsx:" + tag + ">");
	    }
	  }
	
	public SpreadsheetEntry getSpreadsheet(String sheetName) throws IOException, ServiceException {
        URL spreadSheetFeedUrl = new URL(SPREADSHEET_SERVICE_URL);

        SpreadsheetQuery spreadsheetQuery = new SpreadsheetQuery(
        spreadSheetFeedUrl);
        spreadsheetQuery.setTitleQuery(sheetName);
        spreadsheetQuery.setTitleExact(true);
        SpreadsheetFeed spreadsheet = service.getFeed(spreadsheetQuery,
                                               SpreadsheetFeed.class);
        if (spreadsheet.getEntries() != null
                 && spreadsheet.getEntries().size() == 1) {
            return spreadsheet.getEntries().get(0);
        } else {
            throw new RuntimeException("Failed to find spreadsheet with name " + sheetName);
        }
	}
	
	public WorksheetEntry getWorkSheet(String sheetName, String workSheetName) {
	    try {
	        SpreadsheetEntry spreadsheet = getSpreadsheet(sheetName);

	        if (spreadsheet != null) {
	            WorksheetFeed worksheetFeed = service.getFeed(
	                  spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
	            List<WorksheetEntry> worksheets = worksheetFeed.getEntries();

	            for (WorksheetEntry worksheetEntry : worksheets) {
	                 String wktName = worksheetEntry.getTitle().getPlainText();
	                 if (wktName.equals(workSheetName)) {
	                     return worksheetEntry;
	                 }
	             }
	         }
	    } catch (Exception e) {
	    	 throw new RuntimeException("Failed to get worksheet", e);
	    }
	    throw new RuntimeException("Failed to find worksheet");
	}
	
	/**
	 * 
	 * @param resultWorksheetName 
	 * @param spreadSheetName 
	 * @param columns
	 * @param newData
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void writeData(String spreadSheetName, String resultWorksheetName, Map<String, String> columns, List<Map<String, String>> newData) throws IOException, ServiceException {
		try {
			WorksheetEntry worksheet = getWorkSheet(spreadSheetName, resultWorksheetName);
			
			List<ListEntry> entries = readAllEntries(worksheet.getListFeedUrl());
			ListEntry controlEntry = entries.get(1);
			
			Map<String, String> realColumns = new HashMap<>();
			for(String columnHeader: controlEntry.getCustomElements().getTags()){
				realColumns.put(controlEntry.getCustomElements().getValue(columnHeader), columnHeader);
			}
			
			int i = 0;
			for (Map<String, String> record : newData) {
				i++;
				ListEntry existEntry = getListEntry(entries, record, worksheet.getListFeedUrl(), realColumns);
				if (null == existEntry){
					LOG.info("Inserting record " + i);
					ListEntry newEntry = new ListEntry();
					applyNewData(newEntry, realColumns, columns, record);
					service.insert(worksheet.getListFeedUrl(), newEntry);
				} else {
					LOG.info("Updating record " + i);
					applyNewData(existEntry, realColumns, columns, record);
					existEntry.update();
				}

			}
		} catch (Exception e) {
			LOG.error("Failed to write data", e);
		}
	}

	private void applyNewData(ListEntry newEntry, Map<String, String> realColumns, Map<String, String> columns, Map<String, String> record) {
		for(Entry<String, String> column: columns.entrySet()){
			String columnHeader = realColumns.get(column.getValue());
			String newContents = record.get(column.getKey());
			if(null != newContents) {
				LOG.debug(columnHeader + " = " + newContents);
				newEntry.getCustomElements().setValueLocal(columnHeader, newContents);
			}
		}
	}
	
	private ListEntry getListEntry(List<ListEntry> entries, Map<String, String> record, URL listFeedUrl,
			Map<String, String> realColumns) {
		String tank = record.get(TANK);
		String queryColumn = realColumns.get("4");
		for (ListEntry listEntry : entries) {
			if(tank.equals(listEntry.getCustomElements().getValue(queryColumn))){
				return listEntry;
			}
		}
		return null;
	}

	private ListEntry getListEntry(Map<String, String> record, URL listFeedUrl, Map<String, String> realColumns) throws IOException, ServiceException {
		String tank = record.get(TANK);
		String queryColumn = realColumns.get("4");
		String structuredQuery = queryColumn + "=" + tank;
		return query(structuredQuery, listFeedUrl); 
	}

	/**
	 * Performs a full database-like query on the rows.
	 * 
	 * @param structuredQuery
	 *            a query like: name = "Bob" and phone != "555-1212"
	 * @param listFeedUrl
	 * @return 
	 * @throws ServiceException
	 *             when the request causes an error in the Google Spreadsheets
	 *             service.
	 * @throws IOException
	 *             when an error occurs in communication with the Google
	 *             Spreadsheets service.
	 */
	public ListEntry query(String structuredQuery, URL listFeedUrl) throws IOException, ServiceException {
		ListQuery query = new ListQuery(listFeedUrl);
		query.setSpreadsheetQuery(structuredQuery);
		ListFeed feed = service.query(query, ListFeed.class);

		LOG.debug("Results for [" + structuredQuery + "]");

		if(feed.getEntries().isEmpty()) {
			return null;
		} else {
			ListEntry entry = feed.getEntries().get(0);
			return entry;
		}
	}
}
