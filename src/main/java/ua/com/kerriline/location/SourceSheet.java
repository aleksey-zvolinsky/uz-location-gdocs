package ua.com.kerriline.location;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Splitter;
import com.google.common.base.Splitter.MapSplitter;
import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.util.ServiceException;

public class SourceSheet {
	
	private static final Log LOG = LogFactory.getLog(SourceSheet.class);
	private static final String separator = "ДАННЫЕ";
	private static final String[] toDelete = {"-- ИЗ ПОД ----------  ", "== РЕМОНТЫ     ==   : ", "== БЛОК РЕЙСА =====   ", "== ОПИСАНИЕ РЕЙСА ==  "};

	/** Our view of Google Spreadsheets as an authenticated Google user. */
	private SpreadsheetService service;

	/** The URL of the cells feed. */
	private URL cellFeedUrl;
	private List<String> duplicates = new ArrayList<>();
	{
		duplicates.add("СТАНЦИЯ ОПЕРАЦИИ");
	}

	/**
	 * Sets the particular cell at row, col to the specified formula or value.
	 * 
	 * @param row
	 *            the row number, starting with 1
	 * @param col
	 *            the column number, starting with 1
	 * @param formulaOrValue
	 *            the value if it doesn't start with an '=' sign; if it is a
	 *            formula, be careful that cells are specified in R1C1 format
	 *            instead of A1 format.
	 * @throws ServiceException
	 *             when the request causes an error in the Google Spreadsheets
	 *             service.
	 * @throws IOException
	 *             when an error occurs in communication with the Google
	 *             Spreadsheets service.
	 */
	public void setCell(int row, int col, String formulaOrValue) throws IOException, ServiceException {

		CellEntry newEntry = new CellEntry(row, col, formulaOrValue);
		service.insert(cellFeedUrl, newEntry);
		System.out.println("Added!");
	}

	/**
	 * Shows a particular range of cells, limited by minimum/maximum rows and
	 * columns.
	 * 
	 * @param minRow
	 *            the minimum row, inclusive, 1-based
	 * @param maxRow
	 *            the maximum row, inclusive, 1-based
	 * @param minCol
	 *            the minimum column, inclusive, 1-based
	 * @param maxCol
	 *            the maximum column, inclusive, 1-based
	 * @throws ServiceException
	 *             when the request causes an error in the Google Spreadsheets
	 *             service.
	 * @throws IOException
	 *             when an error occurs in communication with the Google
	 *             Spreadsheets service.
	 */
	public void showRange(int minRow, int maxRow, int minCol, int maxCol) throws IOException, ServiceException {
		CellQuery query = new CellQuery(cellFeedUrl);
		query.setMinimumRow(minRow);
		query.setMaximumRow(maxRow);
		query.setMinimumCol(minCol);
		query.setMaximumCol(maxCol);
		CellFeed feed = service.query(query, CellFeed.class);

		for (CellEntry entry : feed.getEntries()) {
			printCell(entry);
		}
	}

	/**
	 * Prints out the specified cell.
	 * 
	 * @param cell
	 *            the cell to print
	 */
	public void printCell(CellEntry cell) {
		String shortId = cell.getId().substring(cell.getId().lastIndexOf('/') + 1);
		LOG.info(" -- Cell(" + shortId + "/" + cell.getTitle().getPlainText() + ") formula("
				+ cell.getCell().getInputValue() + ") numeric(" + cell.getCell().getNumericValue() + ") value("
				+ cell.getCell().getValue() + ")");
	}

	private static final MapSplitter MAP_SPLITTER = Splitter.on('\n')
			.trimResults()
			.omitEmptyStrings()
			.withKeyValueSeparator(Splitter.on(":").trimResults());
	private static final Splitter SPLITTER = Splitter.on("++++++++++++").omitEmptyStrings();
	
	public List<Map<String, String>> text2table(final String body) {
		LOG.info(body);
		
		List<Map<String, String>> res = new ArrayList<Map<String, String>>();
		String text = prepareText(body);
		try {
			SPLITTER.split(text)
				.forEach((s) -> {
					LOG.debug(prepareBlock(s));
					res.add(
						MAP_SPLITTER.split(prepareBlock(s))); 
					} );
		} catch (IllegalArgumentException e) {
			
			if(e.getMessage().contains("Duplicate key")) {
				duplicates.add(e.getMessage().substring(e.getMessage().indexOf("[")+1, e.getMessage().indexOf("]")));
				LOG.debug(e.getMessage());
				text2table(body);
			}
		}
		
		return res;
	}

	private String prepareText(String body) {
		String res = body.replace(separator, "++++++++++++" + separator);
		for(String what: toDelete) {
			res = res.replace(what, "");
		}
		res = res.replace(res.substring(res.indexOf("ПОСЛЕДНИИ ОПЕРАЦИИ"), res.indexOf("КОД ГРУЗА")-2), "");
		
		return res;
	}
	
	private String prepareBlock(final String block) {
		StringBuilder sb = new StringBuilder(block);
		String res = block;

		for(String duplicate: duplicates) {
			sb.setLength(0);
			int i = 0;
			for(String line : Splitter.on("\n").split(res)) {
				List<String> val = Splitter.on(":").trimResults().splitToList(line);
				if(duplicate.equals(val.get(0))) {
					sb.append(duplicate+"-"+i++).append(":").append(val.get(1));	
				} else {
					sb.append(line);
				}
				sb.append("\n");
			}
			res = sb.toString();
		}
		return res;
	}
}
