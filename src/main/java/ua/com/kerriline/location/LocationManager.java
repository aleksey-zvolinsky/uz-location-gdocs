package ua.com.kerriline.location;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.google.gdata.util.ServiceException;

import ua.com.kerriline.location.gdocs.GDocsDrive;
import ua.com.kerriline.location.gdocs.GDocsSheet;
import ua.com.kerriline.location.gdocs.GDocsSheetHelper;
import ua.com.kerriline.location.mail.MailManager;
import ua.com.kerriline.location.mail.MailParser;
import ua.com.kerriline.location.mail.MessageBean;

/**
 * 
 * @author Aleksey
 *
 */
public class LocationManager {

	private static final String REQUEST_NUMBER = "1392";

	private static final Log LOG = LogFactory.getLog(LocationManager.class);
	
	@Autowired MailManager mail;
	@Autowired MailParser source;
	@Autowired GDocsSheet sheet;
	@Autowired GDocsSheetHelper sheetHelper;
	@Autowired GDocsDrive drive;
	

	@Value("${result-mail.to}")
	private String mailTo = "service@kerriline.com.ua";
	
	/**
	 * send requests on email
	 * @return 
	 * @throws IOException 
	 * @throws GeneralSecurityException 
	 * @throws ServiceException 
	 * @throws InterruptedException 
	 */
	public int send() throws GeneralSecurityException, IOException, ServiceException, InterruptedException {
		int mailCount = 0;
		LOG.info("Authorizing");
		sheet.authorize();
		LOG.info("Reading tanks");
		List<Map<String, String>> tanks = sheet.readRawTanks();
		StringBuilder text = new StringBuilder();
		int i = 0;
		for (Map<String, String> tank : tanks) {
			i++;
			text.append(tank.get("вагон")).append("\n");
			if(i >= 150) {
				LOG.info("Sending mail");
				mail.sendMail(REQUEST_NUMBER, text.toString());
				mailCount++;
				Thread.sleep(5000);
				text.setLength(0);
				i = 0;
			}
		}
		LOG.info("Sending mail");
		mail.sendMail(REQUEST_NUMBER, text.toString());
		mailCount++;
		return mailCount;
	}

	public void mail2sheet() throws GeneralSecurityException, IOException, ServiceException {
		LOG.info("Authorizing");
		sheet.authorize();
		LOG.info("Reading tanks");
		List<Map<String, String>> tanks = sheet.readRawTanks();
		LOG.info("Reading column association");
		Map<String, String> columns = sheet.readColumns();
		LOG.info("Reading mails");
		List<MessageBean> messages = mail.search1392Messages();
		Collections.reverse(messages);
		for (MessageBean messageBean : messages) {
			List<Map<String, String>> rawData = source.text2table(messageBean);
			LOG.info("Merging tanks and mail data");
			List<Map<String, String>> newData = source.merge(tanks, rawData);
			LOG.info("Writing data");
			sheet.writeData(columns, newData);
		}
		LOG.info("Sheet updated");
	}
	
	public void removeDeleted() throws GeneralSecurityException, IOException, ServiceException {
		LOG.info("Authorizing");
		sheet.authorize();
		LOG.info("Reading tanks");
		List<Map<String, String>> tanksInput = sheet.readRawTanks();
		LOG.info("Reading column association");
		Map<String, String> columns = sheet.readColumns();
		LOG.info("Reading tanks from result");
		List<Map<String, String>> tanksResult = sheet.readResultTanks();
		Map<String, String> realColumns = sheet.getRealColumns();
		
		List<String> tanksToDelete = source.getObsoleteTanks(tanksInput, tanksResult, realColumns.get("4"));
		for (String tank : tanksToDelete) {
			sheet.removeTank(tank);
		}
		
		

		LOG.info("Sheet updated");
	}

	public void fullTrip() throws GeneralSecurityException, IOException, ServiceException, InterruptedException, MessagingException {
		LOG.info("Remove deleted tanks");
		removeDeleted();
		int requestedMails = send();
		LOG.info("Sleep for 10 minutes before checking mails");
		Thread.sleep(10 * 60 * 1000);
		if(requestedMails > mail.getAll1392MessageCount()) {
			requestedMails = send();
			LOG.info("Sleep for 10 minutes before checking mails");
			Thread.sleep(10 * 60 * 1000);
		}
		mail2sheet();
		exportAndSend();
	}

	public void exportAndSend() throws GeneralSecurityException, IOException, MessagingException {
		File file = drive.export();
		mail.springSendFile(file, mailTo);
		//mail.springSendFile(file, "frendos.a@gmail.com");
	}

}
