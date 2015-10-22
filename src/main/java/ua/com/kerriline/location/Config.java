package ua.com.kerriline.location;

import org.quartz.SchedulerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import ua.com.kerriline.location.gdocs.GDocsDrive;
import ua.com.kerriline.location.gdocs.GDocsSheet;
import ua.com.kerriline.location.gdocs.GDocsSheetHelper;
import ua.com.kerriline.location.mail.MailManager;
import ua.com.kerriline.location.mail.MailParser;

@Configuration
@PropertySource("classpath:config.properties")
@Import(WebController.class)
public class Config {
	
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
	
	@Bean
	public MailManager mailManager() {
		return new MailManager();
	}
	
	@Bean
	public MailParser mailParser() {
		return new MailParser();
	}
	
	@Bean
	public GDocsSheet gDocsSheet() {
		return new GDocsSheet();
	}
	
	@Bean
	public GDocsSheetHelper gDocsSheetHelper() {
		return new GDocsSheetHelper();
	}
	
	
	
	@Bean
	public GDocsDrive gDocsDrive() {
		return new GDocsDrive();
	}
	
	@Bean	
	public SchedulerManager schedulerManager() throws SchedulerException {
		SchedulerManager manager = new SchedulerManager();
		return manager;
	}
	
	@Bean	
	public LocationManager locationManager() throws SchedulerException {
		LocationManager manager = new LocationManager();
		return manager;
	}
}
