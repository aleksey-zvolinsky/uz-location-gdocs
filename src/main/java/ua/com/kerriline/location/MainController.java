package ua.com.kerriline.location;

import javax.inject.Inject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@EnableAutoConfiguration
@Import({Config.class, MvcConfiguration.class})
public class MainController {

	@Inject
	MailManager mail;
	
	@Inject
	SourceSheet source;
	
	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Hello World!";
	}

	@RequestMapping("/mail")
	@ResponseBody
	String mail() {
		return mail.getLast1392().toString();
	}
	
	@RequestMapping("/table")
	@ResponseBody
	String table() {
		return source.text2table(mail.getLast1392().getBody()).toString();
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(MainController.class, args);
	}
}