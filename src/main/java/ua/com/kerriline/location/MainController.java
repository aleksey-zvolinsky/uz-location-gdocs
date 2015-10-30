package ua.com.kerriline.location;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import ua.com.kerriline.location.gdocs.GDocsSheet;
import ua.com.kerriline.location.mail.MailManager;
import ua.com.kerriline.location.mail.MailParser;

@Controller
@EnableAutoConfiguration
@Import({Config.class, MvcConfiguration.class})
public class MainController {

	private static final Log LOG = LogFactory.getLog(MainController.class);


	public static void main(String[] args) throws Exception {
		SpringApplication.run(MainController.class, args);
	}
	
	@Autowired SchedulerManager scheduler;
	@Autowired MailManager mail;
	@Autowired MailParser source;
	@Autowired GDocsSheet sheet;
	@Autowired LocationManager location;
	
	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Hello World!";
	}

	@RequestMapping("/mail")
	@ResponseBody
	String mail() {
		try {
			mail.getAll1392Messages();
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
			location.fulltrip();
			return "done";
		} catch (Exception e) {
			LOG.error("Failed to make sheet", e);
			return "failed";
		}
	}
}