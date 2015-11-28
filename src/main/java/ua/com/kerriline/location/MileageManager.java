package ua.com.kerriline.location;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gdata.util.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ua.com.kerriline.location.data.Mileage;
import ua.com.kerriline.location.data.Tank;
import ua.com.kerriline.location.gdocs.GDocsSheet;
import ua.com.kerriline.location.gdocs.MileageWriter;
import ua.com.kerriline.location.mail.MailManager;
import ua.com.kerriline.location.mail.MessageBean;
import ua.com.kerriline.location.mail.MileageParser;

/**
 * 
 * @author Aleksey
 *
 */
@Component
public class MileageManager {
	
	private static final Log LOG = LogFactory.getLog(LocationManager.class);

	private static final String REQUEST_NUMBER = "2612";

	@Autowired private GDocsSheet sheet;
	@Autowired private MailManager mail;
	@Autowired private MileageParser parser;
	@Autowired private MileageWriter mileageWriter;
	
	/**
	 * @throws ServiceException 
	 * @throws IOException 
	 * @throws GeneralSecurityException 
	 * 
	 */
	public void fullTrip() throws GeneralSecurityException, IOException, ServiceException{
		int retry = 5;
		List<Tank> tanks = readTanks()
				.stream()
				//.limit(20)
				.collect(Collectors.toList());

		List<MessageBean> responses = new ArrayList<>();
		Calendar cal = Calendar.getInstance();
		//cal.add(Calendar.MINUTE, -30);
		Date skipBeforeDate = cal.getTime();
		
		List<Tank> absentTanks = findAbsentTanks(tanks, responses);
		while(!absentTanks.isEmpty() || 0 == retry--) {
			//skipBeforeDate = 
			sendRequest(absentTanks);
			waitForResponse(3 + (absentTanks.size()/30));//minutes
			responses.addAll(readResponses(skipBeforeDate));
			absentTanks = findAbsentTanks(tanks, responses);
			
			// waiting for another mails if they come
			int preSize = 0;
			while(preSize != absentTanks.size()){
				preSize = absentTanks.size();
				waitForResponse(1);//minutes
				responses.addAll(readResponses(skipBeforeDate));
				absentTanks = findAbsentTanks(tanks, responses);
			}
			
		}
		LOG.info("Writing "+ (tanks.size() - absentTanks.size()) +" tanks. Total: " + tanks.size());
		LOG.warn("Missed tanks: " + absentTanks);
		List<Mileage> mileages = parseResponse(tanks, responses);
		writeMileage(mileages);
		File report = exportReport();
		sendFile(report);
	}

	private void writeMileage(List<Mileage> mileages) throws IOException, ServiceException, GeneralSecurityException {
		mileageWriter.writeAll(mileages);
	}

	private List<Mileage> parseResponse(List<Tank> tanks, List<MessageBean> responses) {
		List<String> tanksList = tanks.parallelStream()
			.map(Tank::getTankNumber)
			.collect(Collectors.toList());
		
		List<Mileage> mileages = responses.parallelStream()
			.distinct()
			.map((m) -> parser.parse(m.getSubject(), m.getBody()))
			.filter((m) -> tanksList.contains(m.getTankNumber()))
			.collect(Collectors.toList());
		
		return mileages;
	}

	private List<MessageBean> readResponses(Date skipBeforeDate) {
		return mail.searchMessages(REQUEST_NUMBER, skipBeforeDate);
	}

	/**
	 * 
	 * @param tanks 
	 * @param responses
	 * @return absent tanks in responses
	 */
	private List<Tank> findAbsentTanks(List<Tank> tanks, List<MessageBean> responses) {
		List<String> list = responses.stream()
			.map((m) -> m.getSubject().split("-")[1])
			.distinct()
			.collect(Collectors.toList());
		
		List<Tank> absentTanks = tanks.parallelStream()
			.filter((t) -> !list.contains(t.getTankNumber()))
			.collect(Collectors.toList());
		return absentTanks;
	}

	/** 
	 * TODO: replace explicit wait with 
	 */
	private void waitForResponse(int minutes) {
		try {
			LOG.info("Started waiting " + minutes + " minutes");
			Thread.sleep(minutes*60*1000);
			LOG.info("Finished waiting");
		} catch (InterruptedException e) {
			LOG.error("Failed to make explicit wait", e);
			throw new RuntimeException(e);
		}		
	}

	private Date sendRequest(List<Tank> tanks) throws IOException {
		StringBuilder sb = new StringBuilder();
		tanks.stream()
			.forEach(t -> sb.append(t.getTankNumber()).append("\n"));
		mail.sendMail(REQUEST_NUMBER, sb.toString());
		return new Date();
	}

	private List<Tank> readTanks() throws GeneralSecurityException, IOException, ServiceException {
		LOG.info("Authorizing");
		sheet.authorize();
		LOG.info("Reading tanks");
		List<Tank> tanks = sheet.readTanks();
		return tanks;
	}
	
	private File exportReport() {
		return null;
	}
	
	private void sendFile(File report) {
		// TODO Auto-generated method stub
		
	}
}
