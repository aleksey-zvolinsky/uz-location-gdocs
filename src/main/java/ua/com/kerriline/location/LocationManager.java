package ua.com.kerriline.location;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gdata.util.ServiceException;

import ua.com.kerriline.location.gdocs.GDocsDrive;
import ua.com.kerriline.location.gdocs.GDocsSheet;
import ua.com.kerriline.location.mail.MailManager;
import ua.com.kerriline.location.mail.MailParser;
import ua.com.kerriline.location.mail.MessageBean;

/**
 * 
 * @author Aleksey
 *
 */
public class LocationManager {

	private static final Log LOG = LogFactory.getLog(LocationManager.class);
	
	@Inject	MailManager mail;
	@Inject	MailParser source;
	@Inject	GDocsSheet sheet;
	@Inject GDocsDrive drive;
	
	/**
	 * send requests on email
	 * @throws IOException 
	 * @throws GeneralSecurityException 
	 * @throws ServiceException 
	 */
	public void send() throws GeneralSecurityException, IOException, ServiceException {
		LOG.info("Authorizing");
		sheet.authorize();
		LOG.info("Reading tanks");
		List<Map<String, String>> tanks = sheet.readTanks();
		StringBuilder text = new StringBuilder();
		int i = 0;
		for (Map<String, String> tank : tanks) {
			i++;
			text.append(tank.get("вагон")).append("\n");
			if(i > 150) {
				LOG.info("Sending mail");
				mail.sendMail(text.toString());
				text.setLength(0);
				i = 0;
			}
		}
		LOG.info("Sending mail");
		mail.sendMail(text.toString());
	}

	public void mail2sheet() throws GeneralSecurityException, IOException, ServiceException {
		LOG.info("Authorizing");
		sheet.authorize();
		LOG.info("Reading tanks");
		List<Map<String, String>> tanks = sheet.readTanks();
		LOG.info("Reading column association");
		Map<String, String> columns = sheet.readColumns();
		LOG.info("Reading mails");
		List<MessageBean> messages = mail.getAll1392Messages();
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

	public void fulltrip() throws GeneralSecurityException, IOException, ServiceException, InterruptedException {
		//send();
		LOG.info("Sleep for 10 minutes before checking mails");
		//Thread.currentThread().sleep(5 * 60 * 1000);
		mail2sheet();
		exportAndSend();
	}

	public void exportAndSend() throws GeneralSecurityException, IOException {
		File file = drive.export();
		mail.sendFile(file, "service@kerriline.com.ua");
		//mail.sendFile(file, "frendos.a@gmail.com");
	}

}
