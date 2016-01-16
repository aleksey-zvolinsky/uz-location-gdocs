package ua.com.kerriline.location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import ua.com.kerriline.location.gdocs.GDocsSheet;
import ua.com.kerriline.location.mail.MailManager;
import ua.com.kerriline.location.mail.MailParser;

@Controller
@SpringBootApplication
public class MainController {

	private static final Logger LOG = LoggerFactory.getLogger(MainController.class);


	public static void main(String[] args) throws Exception {
		SpringApplication.run(MainController.class, args);
	}
	
	@Autowired SchedulerManager scheduler;
	@Autowired MailManager mail;
	@Autowired MailParser source;
	@Autowired GDocsSheet sheet;
	@Autowired LocationManager location;
	@Autowired MileageManager mileage;
	
	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Hello World!";
	}

	@RequestMapping("/mail")
	@ResponseBody
	String mail() {
		try {
			mail.search1392Messages();
			return "done";
		} catch (Exception e) {
			LOG.error("Failed to get mails", e);
			return "failed";
		}
		
	}
	
	
	@RequestMapping("/send")
	@ResponseBody
	String send() {
		try {
			location.send();
			return "done";
		} catch (Exception e) {
			LOG.error("Failed to get mails", e);
			return "failed";
		}
		
	}
	
	
	@RequestMapping("/table")
	@ResponseBody
	String table() {
		return source.text2table(mail.getLast1392()).toString();
	}
	
	@RequestMapping("/sheet")
	@ResponseBody
	String sheet() {
		try {
			location.removeDeleted();
			location.mail2sheet();
			return "done";
		} catch (Exception e) {
			LOG.error("Failed to make sheet", e);
			return "failed";
		}
	}
	
	@RequestMapping("/export")
	@ResponseBody
	String exportAndSend() {
		try {
			location.exportAndSend();
			return "done";
		} catch (Exception e) {
			LOG.error("Failed to make sheet", e);
			return "failed";
		}
	}
	
	
	@RequestMapping("/full")
	@ResponseBody
	String full() {
		try {
			location.fullTrip();
			return "done";
		} catch (Exception e) {
			LOG.error("Failed to make sheet", e);
			return "failed";
		}
	}
	
	@RequestMapping("/mileage")
	@ResponseBody
	String mileage() {
		try {
			LOG.info("Requested mileage update");
			mileage.fullTrip();
			LOG.info("Mileage was successfully updated");
		} catch (Exception e) {
			LOG.error("Failed to get mileage", e);
			return "Failed to get mileage";
		}
		return "done";
	}
	
	@RequestMapping("/test/mail-errors")
	@ResponseBody
	String mailErrors() {
		try {
			for(int i=0;i<1000;i++){
				LOG.info("Info {} message", i);
				LOG.debug("Debug {} message", i);
				LOG.trace("Trace {} message", i);
			}
			LOG.error("This message should be send to mail");
			return "done";
		} catch (Exception e) {
			LOG.error("Failed to make sheet", e);
			return "failed";
		}
	}
}