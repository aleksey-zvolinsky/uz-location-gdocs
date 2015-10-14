package ua.com.kerriline.location;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
	
	@Bean
	public MailManager mailManager() {
		return new MailManager();
	}
	
	@Bean
	public SourceSheet sourceSheet() {
		return new SourceSheet();
	}
}
